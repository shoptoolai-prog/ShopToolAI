package com.example.data.service.mock

import com.example.domain.service.ReviewAnalysisService
import com.example.domain.service.ReviewSummary
import kotlinx.coroutines.delay

class MockReviewAnalysisService : ReviewAnalysisService {
    override suspend fun analyzeReviews(productId: String): ReviewSummary {
        // Simulate LLM summary delay
        delay(700)
        
        return when {
            productId.contains("keyboard") -> ReviewSummary(
                positiveSummary = "Exceptional tactile click and heavy-duty chassis weight. Fluid multi-device Bluetooth switching.",
                negativeSummary = "Standard keycaps show finger grease quickly; battery life depletes faster with full RGB backlight enabled."
            )
            productId.contains("monitor") || productId.contains("light") -> ReviewSummary(
                positiveSummary = "Perfect glare-free asymmetrical beam. Auto-dimming ambient light sensor keeps eye strain minimized.",
                negativeSummary = "The wireless desk puck controller requires frequent battery replacements and can be slightly unresponsive."
            )
            productId.contains("mat") -> ReviewSummary(
                positiveSummary = "Luxurious thick merino wool composition that stabilizes keyboard acoustics beautifully.",
                negativeSummary = "Can be slightly scratchy on bare forearms during long sessions; occasionally pills and collects lint."
            )
            productId.contains("diffuser") -> ReviewSummary(
                positiveSummary = "Premium heavy porcelain design that looks like high-end art. Consistent, quiet scent throw with fine mist.",
                negativeSummary = "Water reservoir capacity is somewhat limited; requires frequent refilling for continuous all-day run times."
            )
            productId.contains("lamp") -> ReviewSummary(
                positiveSummary = "Exquisite design classic. Delicate matte pleated shade pairs with a sturdy solid wooden stand.",
                negativeSummary = "The shade is made of heavy paper and can dent or stain permanently if handled carelessly."
            )
            productId.contains("sneakers") -> ReviewSummary(
                positiveSummary = "Extremely versatile vintage design. Excellent arch support and soft genuine leather paneling.",
                negativeSummary = "Noticeably heavy and bulky compared to modern runners; has a somewhat long break-in period."
            )
            productId.contains("sunglasses") -> ReviewSummary(
                positiveSummary = "Ultra-lightweight bio-acetate frame. Premium polarized lenses with outstanding clarity and UV block.",
                negativeSummary = "The hinges feel a bit tight out of the box; does not come with a rigid hard-shell travel case."
            )
            productId.contains("shirt") || productId.contains("overshirt") -> ReviewSummary(
                positiveSummary = "Incredibly breathable pure organic linen weave. Stylish relaxed drape that fits loosely but smartly.",
                negativeSummary = "Wrinkles almost instantly after sitting down; instructions require dedicated cold delicate washing."
            )
            productId.contains("mug") -> ReviewSummary(
                positiveSummary = "Flawless temperature maintenance down to the exact degree. Beautiful matte texture feel in-hand.",
                negativeSummary = "Must be hand washed only; the battery lasts barely 80 minutes away from the circular charging coaster."
            )
            else -> ReviewSummary(
                positiveSummary = "Highly rated design that delivers premium build material and is extremely simple to configure out-of-box.",
                negativeSummary = "Slightly higher premium pricing compared to generic online alternatives."
            )
        }
    }
}
