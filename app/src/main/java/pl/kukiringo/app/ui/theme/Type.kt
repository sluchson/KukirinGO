package pl.kukiringo.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import pl.kukiringo.app.R

// Exo 2 (pochylona) — ten sam krój co w logo. Licencja: docs/licencje/Exo2-OFL.txt
val Exo2 = FontFamily(
    Font(R.font.exo2_bold_italic, FontWeight.Bold, FontStyle.Italic),
    Font(R.font.exo2_extrabold_italic, FontWeight.ExtraBold, FontStyle.Italic),
)

private val Base = Typography()

val KukirinTypography = Typography(
    displaySmall = TextStyle(
        fontFamily = Exo2,
        fontWeight = FontWeight.ExtraBold,
        fontStyle = FontStyle.Italic,
        fontSize = 44.sp,
        lineHeight = 48.sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = Exo2,
        fontWeight = FontWeight.ExtraBold,
        fontStyle = FontStyle.Italic,
        fontSize = 30.sp,
        lineHeight = 36.sp,
    ),
    titleLarge = TextStyle(
        fontFamily = Exo2,
        fontWeight = FontWeight.ExtraBold,
        fontStyle = FontStyle.Italic,
        fontSize = 22.sp,
        lineHeight = 28.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = Exo2,
        fontWeight = FontWeight.Bold,
        fontStyle = FontStyle.Italic,
        fontSize = 18.sp,
        lineHeight = 24.sp,
    ),
    bodyLarge = Base.bodyLarge,
    bodyMedium = Base.bodyMedium,
    bodySmall = Base.bodySmall,
    labelLarge = Base.labelLarge.copy(fontWeight = FontWeight.Bold),
    labelMedium = Base.labelMedium,
)
