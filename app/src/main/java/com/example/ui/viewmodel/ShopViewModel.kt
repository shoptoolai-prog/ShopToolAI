package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.HistoryEntity
import com.example.data.model.Product
import com.example.data.model.PriceSource
import com.example.data.repository.HistoryRepository
import com.example.data.util.NetworkUtils
import com.example.data.util.RetryUtils
import com.example.data.util.AiCacheManager
import com.example.domain.engine.*
import com.example.domain.service.*
import com.example.domain.model.ShoppingProduct
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface AnalysisUiState {
    object Idle : AnalysisUiState
    data class Loading(val message: String) : AnalysisUiState
    data class Success(val url: String, val products: List<Product>) : AnalysisUiState
    data class Error(val message: String) : AnalysisUiState
}

sealed interface DecisionUiState {
    object Idle : DecisionUiState
    object Loading : DecisionUiState
    data class Success(val result: com.example.domain.decision.DecisionResult) : DecisionUiState
    data class Error(val message: String) : DecisionUiState
}

class ShopViewModel(
    application: Application,
    private val repository: HistoryRepository,
    val shoppingRepository: com.example.domain.repository.shopping.ShoppingRepository,
    val priceRepository: com.example.domain.repository.shopping.PriceRepository,
    val reviewRepository: com.example.domain.repository.shopping.ReviewRepository,
    val sellerRepository: com.example.domain.repository.shopping.SellerRepository,
    val shoppingRecommendationRepository: com.example.domain.repository.shopping.RecommendationRepository,
    val shoppingProviderManager: com.example.domain.shopping.ShoppingProviderManager,
    val productSearchEngine: com.example.domain.shopping.ProductSearchEngine,
    val productMatchingEngine: com.example.domain.shopping.ProductMatchingEngine,
    val priceComparisonEngine: com.example.domain.shopping.PriceComparisonEngine,
    val discountEngine: com.example.domain.shopping.DiscountEngine,
    val couponEngine: com.example.domain.shopping.CouponEngine,
    val availabilityEngine: com.example.domain.shopping.AvailabilityEngine,
    val deliveryEstimationEngine: com.example.domain.shopping.DeliveryEstimationEngine,
    val reviewAggregationEngine: com.example.domain.shopping.ReviewAggregationEngine,
    val sellerRankingEngine: com.example.domain.shopping.SellerRankingEngine,
    val productScoringEngine: com.example.domain.shopping.ProductScoringEngine,
    val aiDecisionManager: com.example.domain.decision.AiDecisionManager
) : AndroidViewModel(application) {

    // Bottom Navigation tab
    private val _currentTab = MutableStateFlow("home")
    val currentTab: StateFlow<String> = _currentTab.asStateFlow()

    // AI Decision Engine State
    private val _decisionState = MutableStateFlow<DecisionUiState>(DecisionUiState.Idle)
    val decisionState: StateFlow<DecisionUiState> = _decisionState.asStateFlow()

    // Analysis State
    private val _analysisState = MutableStateFlow<AnalysisUiState>(AnalysisUiState.Idle)
    val analysisState: StateFlow<AnalysisUiState> = _analysisState.asStateFlow()

    // Search History flow
    val historyState: StateFlow<List<HistoryEntity>> = repository.allHistory
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // App Preferences
    private val _isDarkMode = MutableStateFlow(true) // Dark mode first as requested!
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private val _currentLanguage = MutableStateFlow("English")
    val currentLanguage: StateFlow<String> = _currentLanguage.asStateFlow()

    fun selectTab(tab: String) {
        _currentTab.value = tab
    }

    fun toggleTheme() {
        _isDarkMode.value = !_isDarkMode.value
    }

    fun setLanguage(lang: String) {
        _currentLanguage.value = lang
    }

    fun analyzeReel(url: String) {
        val trimmedUrl = url.trim()
        if (trimmedUrl.isBlank()) {
            _analysisState.value = AnalysisUiState.Error("Please paste an Instagram Reel URL to begin.")
            return
        }

        // Validate using the engine analyzer first
        val isValidFormat = repository.reelAnalyzerEngine.isValidReelUrl(trimmedUrl)
        if (!isValidFormat) {
            _analysisState.value = AnalysisUiState.Error("Invalid link. Please enter a valid Instagram Reel URL (e.g., https://www.instagram.com/reel/...)")
            return
        }

        viewModelScope.launch {
            try {
                // 1. Internet connection check
                if (!NetworkUtils.isInternetAvailable(getApplication())) {
                    _analysisState.value = AnalysisUiState.Error("No internet connection detected. Please check your network and try again.")
                    return@launch
                }

                // 2. Caching Support - check if we already have the analysis for this URL
                val cachedResult = AiCacheManager.get(trimmedUrl)
                if (cachedResult != null) {
                    Log.d("ShopViewModel", "Found cached analysis results for URL: $trimmedUrl")
                    _analysisState.value = AnalysisUiState.Loading("Serving results from cache...")
                    kotlinx.coroutines.delay(600)
                    _analysisState.value = AnalysisUiState.Success(trimmedUrl, cachedResult)
                    return@launch
                }

                Log.d("ShopViewModel", "Starting Phase 4 AI Workflow for: $trimmedUrl")

                // Execute Phase 4 Workflow on Dispatchers.IO for background processing:
                val finalProducts = withContext(Dispatchers.IO) {
                    
                    // Workflow 1, 2, & 3: Run the Reel Processing Manager to validate, download, extract & cache
                    val extractedFrames = mutableListOf<com.example.domain.engine.ExtractedFrame>()
                    repository.reelProcessingManager.processReel(trimmedUrl).collect { state ->
                        withContext(Dispatchers.Main) {
                            when (state) {
                                is ReelProcessingState.CheckingNetwork -> {
                                    _analysisState.value = AnalysisUiState.Loading("1/8: Checking Network Connectivity...")
                                }
                                is ReelProcessingState.ValidatingUrl -> {
                                    _analysisState.value = AnalysisUiState.Loading("1/8: Validating & Parsing Reel URL...")
                                }
                                is ReelProcessingState.Downloading -> {
                                    val pct = (state.progress * 100).toInt()
                                    _analysisState.value = AnalysisUiState.Loading("2/8: Downloading Reel Content ($pct%)...")
                                }
                                is ReelProcessingState.ExtractingFrames -> {
                                    val pct = (state.progress * 100).toInt()
                                    _analysisState.value = AnalysisUiState.Loading("3/8: Extracting Speed-Optimized Keyframes ($pct%)...")
                                }
                                is ReelProcessingState.Success -> {
                                    extractedFrames.clear()
                                    extractedFrames.addAll(state.frames)
                                }
                                is ReelProcessingState.Error -> {
                                    throw IllegalStateException(state.message)
                                }
                                else -> {}
                            }
                        }
                    }

                    if (extractedFrames.isEmpty()) {
                        throw IllegalStateException("Frame extraction yielded no frames.")
                    }

                    // Workflow 4: Detect Products (Object & Fashion)
                    withContext(Dispatchers.Main) {
                        _analysisState.value = AnalysisUiState.Loading("4/8: Running Visual AI Object & Fashion Detection...")
                    }
                    coroutineScope {
                        val objectsDeferred = async {
                            RetryUtils.retryWithTimeout(operationName = "ObjectDetectionEngine.detectObjects") {
                                repository.objectDetectionEngine.detectObjects(extractedFrames)
                            }
                        }
                        val fashionDeferred = async {
                            RetryUtils.retryWithTimeout(operationName = "FashionDetectionEngine.detectFashionItems") {
                                repository.fashionDetectionEngine.detectFashionItems(extractedFrames)
                            }
                        }
                        val detectedObjects = objectsDeferred.await()
                        val detectedFashion = fashionDeferred.await()

                        // Workflow 5: Match Products
                        withContext(Dispatchers.Main) {
                            _analysisState.value = AnalysisUiState.Loading("5/8: Querying Product Match Indexes...")
                        }
                        val matchedResults = RetryUtils.retryWithTimeout(
                            operationName = "ProductMatchingEngine.findMatches"
                        ) {
                            repository.productMatchingEngine.findMatches(detectedObjects, detectedFashion)
                        }

                        val initialProducts = matchedResults.map { it.matchedProduct }

                        // Workflow 6 & 7: Compare Prices & Analyze Reviews (Enriching final products)
                        withContext(Dispatchers.Main) {
                            _analysisState.value = AnalysisUiState.Loading("6/8: Verifying Retail Price Comparisons...")
                        }
                        
                        val enrichedProducts = initialProducts.mapIndexed { index, product ->
                            // Show review state transition on step 7
                            if (index == initialProducts.size / 2) {
                                withContext(Dispatchers.Main) {
                                    _analysisState.value = AnalysisUiState.Loading("7/8: Summarizing Customer Sentiment & Reviews...")
                                }
                            }

                            val prices = RetryUtils.retryWithTimeout(operationName = "PriceComparisonEngine.comparePrices") {
                                repository.priceComparisonEngine.comparePrices(product.id, product.name, product.lowestPrice)
                            }
                            
                            val sentimentSummary = RetryUtils.retryWithTimeout(operationName = "ReviewAnalysisEngine.analyzeSentiment") {
                                repository.reviewAnalysisEngine.analyzeSentiment(product.id, product.name)
                            }

                            product.copy(
                                priceComparison = prices,
                                positiveReviewSummary = sentimentSummary.prosSummary,
                                negativeReviewSummary = sentimentSummary.consSummary,
                                lowestPrice = prices.firstOrNull { it.isLowest }?.price ?: product.lowestPrice
                            )
                        }

                        // Workflow 8: Generate Final Results
                        withContext(Dispatchers.Main) {
                            _analysisState.value = AnalysisUiState.Loading("8/8: Assembling Curated Product Catalog...")
                        }
                        kotlinx.coroutines.delay(400)

                        enrichedProducts
                    }
                }

                // Caching the final result
                AiCacheManager.put(trimmedUrl, finalProducts)

                // Save to Room DB for local persistence history!
                repository.insertHistory(trimmedUrl, finalProducts)

                Log.d("ShopViewModel", "Phase 4 AI Workflow finished successfully for: $trimmedUrl")
                _analysisState.value = AnalysisUiState.Success(trimmedUrl, finalProducts)

            } catch (e: Exception) {
                Log.e("ShopViewModel", "Error during AI Workflow analysis", e)
                _analysisState.value = AnalysisUiState.Error(
                    e.message ?: "Analysis failed. Please check the URL format or try again later."
                )
            }
        }
    }

    fun resetAnalysis() {
        _analysisState.value = AnalysisUiState.Idle
    }

    fun loadPastResult(url: String, products: List<Product>) {
        _analysisState.value = AnalysisUiState.Success(url, products)
    }

    fun deleteHistoryItem(id: Int) {
        viewModelScope.launch {
            repository.deleteHistory(id)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    fun runDecisionEngine(
        detectedQuery: String,
        queryAttributes: List<String>,
        candidates: List<ShoppingProduct>,
        userBudget: Double? = null,
        preferredBrand: String? = null
    ) {
        viewModelScope.launch {
            _decisionState.value = DecisionUiState.Loading
            try {
                val result = aiDecisionManager.processAndDecisionQuery(
                    detectedQuery = detectedQuery,
                    queryAttributes = queryAttributes,
                    candidates = candidates,
                    userBudget = userBudget,
                    preferredBrand = preferredBrand
                )
                _decisionState.value = DecisionUiState.Success(result)
            } catch (e: Exception) {
                Log.e("ShopViewModel", "Decision Engine failed", e)
                _decisionState.value = DecisionUiState.Error(e.message ?: "Unknown error in Decision Engine")
            }
        }
    }

    // Factory to instantiate viewmodel properly
    companion object {
        fun provideFactory(application: Application): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val database = AppDatabase.getDatabase(application)
                
                // Real-world integration client and provider configuration injection:
                val networkClient = com.example.data.integration.NetworkClient(application)
                val providerManager = com.example.data.integration.ProviderManagerImpl(networkClient)
                
                val objectEngine = com.example.data.engine.integration.IntegrationObjectDetectionEngine(providerManager)
                val fashionEngine = com.example.data.engine.integration.IntegrationFashionDetectionEngine(providerManager)
                val productEngine = com.example.data.engine.integration.IntegrationProductMatchingEngine(providerManager)
                val priceEngine = com.example.data.engine.integration.IntegrationPriceComparisonEngine(providerManager)
                val reviewEngine = com.example.data.engine.integration.IntegrationReviewAnalysisEngine(providerManager)
                val recommendationEngine = com.example.data.engine.integration.IntegrationSimilarProductRecommendationEngine(providerManager)
                
                val reelManager = com.example.data.service.ReelProcessingManagerImpl(application)
                
                // Initialize Repositories
                val intelProductRepo = com.example.data.repository.ProductRepositoryImpl(application)
                val intelBrandRepo = com.example.data.repository.BrandRepositoryImpl(application)
                val intelSearchRepo = com.example.data.repository.SearchRepositoryImpl(application)
                val intelRecommendationRepo = com.example.data.repository.RecommendationRepositoryImpl(application)

                // Initialize Modules
                val detectionModule = com.example.data.intelligence.ProductDetectionModuleImpl(providerManager)
                val brandModule = com.example.data.intelligence.BrandRecognitionModuleImpl()
                val colorModule = com.example.data.intelligence.ColorDetectionModuleImpl()
                val categoryModule = com.example.data.intelligence.CategoryDetectionModuleImpl()
                val similarityModule = com.example.data.intelligence.SimilarProductFinderModuleImpl(providerManager)
                val duplicateFilterModule = com.example.data.intelligence.DuplicateProductFilterModuleImpl()
                val rankingModule = com.example.data.intelligence.ProductRankingModuleImpl()
                val confidenceModule = com.example.data.intelligence.ConfidenceScoreModuleImpl()

                // Initialize Product Intelligence Manager
                val productIntelManager = com.example.data.intelligence.ProductIntelligenceManagerImpl(
                    detectionModule = detectionModule,
                    brandModule = brandModule,
                    colorModule = colorModule,
                    categoryModule = categoryModule,
                    similarityModule = similarityModule,
                    duplicateFilterModule = duplicateFilterModule,
                    rankingModule = rankingModule,
                    confidenceModule = confidenceModule,
                    productRepository = intelProductRepo,
                    brandRepository = intelBrandRepo,
                    searchRepository = intelSearchRepo,
                    recommendationRepository = intelRecommendationRepo
                )

                // 1. Initialize Shopping Repositories
                val shoppingProdDao = database.shoppingProductDao()
                val shoppingRepo = com.example.data.repository.shopping.ShoppingRepositoryImpl(application, shoppingProdDao, networkClient)
                val priceRepo = com.example.data.repository.shopping.PriceRepositoryImpl(application, networkClient)
                val reviewRepo = com.example.data.repository.shopping.ReviewRepositoryImpl(application, networkClient)
                val sellerRepo = com.example.data.repository.shopping.SellerRepositoryImpl(application, networkClient)
                val shoppingRecommendationRepo = com.example.data.repository.shopping.RecommendationRepositoryImpl(application, networkClient)

                // 2. Initialize Shopping Engines
                val shoppingProvManager = com.example.data.shopping.ShoppingProviderManagerImpl()
                val prodSearchEngine = com.example.data.shopping.ProductSearchEngineImpl(shoppingProvManager)
                val prodMatchingEngine = com.example.data.shopping.ProductMatchingEngineImpl()
                val priceCompEngine = com.example.data.shopping.PriceComparisonEngineImpl()
                val discEngine = com.example.data.shopping.DiscountEngineImpl()
                val coupEngine = com.example.data.shopping.CouponEngineImpl()
                val availEngine = com.example.data.shopping.AvailabilityEngineImpl()
                val deliveryEstEngine = com.example.data.shopping.DeliveryEstimationEngineImpl()
                val revAggEngine = com.example.data.shopping.ReviewAggregationEngineImpl()
                val sellRankEngine = com.example.data.shopping.SellerRankingEngineImpl()
                val prodScoringEngine = com.example.data.shopping.ProductScoringEngineImpl(shoppingRecommendationRepo)

                val decisionConfidenceEngine = com.example.data.decision.ConfidenceScoreEngineImpl()
                val decisionSimilarityEngine = com.example.data.decision.ProductSimilarityEngineImpl()
                val decisionRankingEngine = com.example.data.decision.ProductRankingEngineImpl()
                val decisionDuplicateEngine = com.example.data.decision.DuplicateRemovalEngineImpl()
                val decisionBestMatchEngine = com.example.data.decision.BestMatchEngineImpl()
                val decisionBudgetEngine = com.example.data.decision.BudgetRecommendationEngineImpl()
                val decisionPremiumEngine = com.example.data.decision.PremiumRecommendationEngineImpl()
                val decisionTrendingEngine = com.example.data.decision.TrendingProductEngineImpl()
                val decisionSmartEngine = com.example.data.decision.SmartRecommendationEngineImpl()

                val decisionManager = com.example.data.decision.AiDecisionManagerImpl(
                    context = application,
                    productDao = shoppingProdDao,
                    rankingEngine = decisionRankingEngine,
                    similarityEngine = decisionSimilarityEngine,
                    confidenceEngine = decisionConfidenceEngine,
                    duplicateEngine = decisionDuplicateEngine,
                    bestMatchEngine = decisionBestMatchEngine,
                    budgetEngine = decisionBudgetEngine,
                    premiumEngine = decisionPremiumEngine,
                    trendingEngine = decisionTrendingEngine,
                    smartEngine = decisionSmartEngine
                )

                val repository = HistoryRepository(
                    historyDao = database.historyDao(),
                    objectDetectionEngine = objectEngine,
                    fashionDetectionEngine = fashionEngine,
                    productMatchingEngine = productEngine,
                    priceComparisonEngine = priceEngine,
                    reviewAnalysisEngine = reviewEngine,
                    similarProductRecommendationEngine = recommendationEngine,
                    reelProcessingManager = reelManager,
                    productIntelligenceManager = productIntelManager
                )
                return ShopViewModel(
                    application = application,
                    repository = repository,
                    shoppingRepository = shoppingRepo,
                    priceRepository = priceRepo,
                    reviewRepository = reviewRepo,
                    sellerRepository = sellerRepo,
                    shoppingRecommendationRepository = shoppingRecommendationRepo,
                    shoppingProviderManager = shoppingProvManager,
                    productSearchEngine = prodSearchEngine,
                    productMatchingEngine = prodMatchingEngine,
                    priceComparisonEngine = priceCompEngine,
                    discountEngine = discEngine,
                    couponEngine = coupEngine,
                    availabilityEngine = availEngine,
                    deliveryEstimationEngine = deliveryEstEngine,
                    reviewAggregationEngine = revAggEngine,
                    sellerRankingEngine = sellRankEngine,
                    productScoringEngine = prodScoringEngine,
                    aiDecisionManager = decisionManager
                ) as T
            }
        }
    }
}
