package com.shopverse.cmp.screen.product

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import com.shopverse.cmp.core.architecture.Route
import com.shopverse.cmp.core.theme.AddToCartBlue
import com.shopverse.cmp.core.theme.DiscountRed
import com.shopverse.cmp.core.theme.PriceMuted
import com.shopverse.cmp.core.theme.RatingStar
import com.shopverse.cmp.core.theme.TextGray
import com.shopverse.cmp.model.Product
import com.shopverse.cmp.screen.Screen
import com.shopverse.cmp.screen.component.BackButton
import com.shopverse.cmp.screen.component.DiscountBadge
import com.shopverse.cmp.screen.component.discountPercent
import com.shopverse.cmp.screen.component.priceLabel
import com.shopverse.cmp.screen.component.ratingLabel
import com.shopverse.cmp.screen.main.MAIN_SELECT_TAB_CART
import com.shopverse.cmp.screen.main.MAIN_SELECT_TAB_KEY
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import shopversecmp.composeapp.generated.resources.Res
import shopversecmp.composeapp.generated.resources.ic_star

private const val LOW_STOCK_THRESHOLD = 10

/**
 * Product detail, ported 1:1 from the Android app's ProductDetailView: cover art with discount
 * badge, title/maker/rating, genre+platform chips, facts card, description, and a bottom bar
 * with price + Add to Cart / Go to Cart / Out of Stock action.
 */
@Composable
fun ProductRoute(
    navController: NavHostController,
    slug: String,
) {
    val viewModel = koinViewModel<ProductDetailViewModel> { parametersOf(slug) }
    Route(
        viewModel = viewModel,
        topBar = { BackButton(onClick = { navController.popBackStack() }) },
        onRetry = viewModel::load,
        onEffect = { effect ->
            when (effect) {
                ProductDetailEffect.GoToCart -> {
                    // Ask the Main tab host to switch to Cart, then pop back to it — the CMP
                    // equivalent of Android's navigateToNavigator(selectTabTag = TAB_CART).
                    runCatching { navController.getBackStackEntry(Screen.Main) }
                        .getOrNull()
                        ?.savedStateHandle
                        ?.set(MAIN_SELECT_TAB_KEY, MAIN_SELECT_TAB_CART)
                    navController.popBackStack()
                }
            }
        },
    ) { model ->
        ProductDetailContent(
            model = model,
            onAddToCart = viewModel::addToCart,
            onGoToCart = viewModel::goToCart,
        )
    }
}

@Composable
private fun ProductDetailContent(
    model: ProductDetailModel,
    onAddToCart: () -> Unit,
    onGoToCart: () -> Unit,
) {
    val product = model.product
    Column(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 12.dp),
        ) {
            CoverImage(product)

            Spacer(Modifier.height(14.dp))
            Text(
                text = product.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
            )

            val maker = listOf(product.developer, product.publisher)
                .filter { it.isNotBlank() }
                .distinct()
                .joinToString(" · ")
            if (maker.isNotEmpty()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = maker,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextGray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            product.ratingAvg?.let { rating ->
                Spacer(Modifier.height(8.dp))
                RatingRow(rating = rating, count = product.ratingCount ?: 0)
            }

            val tags = buildList {
                if (product.genre.isNotBlank()) add(product.genre)
                addAll(product.platforms.map { it.platformLabel() })
            }
            if (tags.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                TagsRow(tags)
            }

            Spacer(Modifier.height(12.dp))
            FactsCard(product)

            Spacer(Modifier.height(16.dp))
            Text(
                text = "About",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = product.description,
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 22.sp,
                color = TextGray,
                modifier = Modifier.padding(bottom = 16.dp),
            )
        }

        BottomBar(model = model, onAddToCart = onAddToCart, onGoToCart = onGoToCart)
    }
}

@Composable
private fun CoverImage(product: Product) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(400.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
    ) {
        AsyncImage(
            model = product.image,
            contentDescription = product.title,
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize(),
        )
        if (product.isOnSale) {
            val percent = discountPercent(product.oldPrice!!, product.currentPrice)
            if (percent > 0) {
                DiscountBadge(
                    percent = percent,
                    modifier = Modifier.align(Alignment.TopStart).padding(8.dp),
                )
            }
        }
    }
}

@Composable
private fun RatingRow(rating: Double, count: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            painter = painterResource(Res.drawable.ic_star),
            contentDescription = null,
            tint = RatingStar,
            modifier = Modifier.size(14.dp),
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = ratingLabel(rating),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        if (count > 0) {
            Spacer(Modifier.width(4.dp))
            Text(
                text = "($count)",
                style = MaterialTheme.typography.bodySmall,
                color = PriceMuted,
            )
        }
    }
}

@Composable
private fun TagsRow(tags: List<String>) {
    Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        tags.forEachIndexed { index, tag ->
            val highlighted = index == 0
            Text(
                text = tag,
                style = MaterialTheme.typography.labelMedium,
                color = if (highlighted) Color.White else MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                modifier = Modifier
                    .background(
                        color = if (highlighted) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        },
                        shape = RoundedCornerShape(50),
                    )
                    .padding(horizontal = 12.dp, vertical = 6.dp),
            )
        }
    }
}

@Composable
private fun FactsCard(product: Product) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surface),
    ) {
        FactRow(label = "Release date", value = formatDate(product.releaseDate))
        product.stock?.let { stock ->
            HorizontalDivider(
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.padding(start = 16.dp),
            )
            when {
                stock <= 0 -> FactRow("Availability", "Out of stock", DiscountRed)
                stock < LOW_STOCK_THRESHOLD ->
                    FactRow("Availability", "Only $stock left", DiscountRed)
                else -> FactRow("Availability", "In stock", TextGray)
            }
        }
    }
}

@Composable
private fun FactRow(label: String, value: String, valueColor: Color? = null) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 48.dp)
            .padding(horizontal = 16.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = TextGray,
            maxLines = 1,
            modifier = Modifier.weight(1f).padding(end = 16.dp),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = valueColor ?: MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
        )
    }
}

@Composable
private fun BottomBar(
    model: ProductDetailModel,
    onAddToCart: () -> Unit,
    onGoToCart: () -> Unit,
) {
    val product = model.product
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 10.dp),
    ) {
        Column(Modifier.padding(end = 16.dp)) {
            Text(
                text = "${priceLabel(product.currentPrice)} ${product.currency}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
            )
            if (product.isOnSale) {
                Text(
                    text = "${priceLabel(product.oldPrice!!)} ${product.currency}",
                    style = MaterialTheme.typography.bodySmall,
                    color = PriceMuted,
                    textDecoration = TextDecoration.LineThrough,
                    maxLines = 1,
                )
            }
        }
        Button(
            onClick = if (model.isInCart) onGoToCart else onAddToCart,
            enabled = !model.isOutOfStock,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AddToCartBlue,
                contentColor = Color.White,
                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                disabledContentColor = TextGray,
            ),
            modifier = Modifier.weight(1f).height(48.dp),
        ) {
            Text(
                text = when {
                    model.isOutOfStock -> "Out of Stock"
                    model.isInCart -> "Go to Cart"
                    else -> "Add to Cart"
                },
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
            )
        }
    }
}

private val MONTHS = listOf(
    "Jan", "Feb", "Mar", "Apr", "May", "Jun",
    "Jul", "Aug", "Sep", "Oct", "Nov", "Dec",
)

/** "2020-12-10" → "Dec 10, 2020"; falls back to the raw string (Android parity). */
private fun formatDate(releaseDate: String): String = runCatching {
    val date = LocalDate.parse(releaseDate)
    "${MONTHS[date.monthNumber - 1]} ${date.dayOfMonth}, ${date.year}"
}.getOrDefault(releaseDate)

/** "PC" stays upper-cased, "PS5"/"XBOX_SERIES" etc. get title-cased words (Android parity). */
private fun String.platformLabel(): String =
    replace('_', ' ').lowercase().split(' ').joinToString(" ") { word ->
        word.replaceFirstChar { it.uppercase() }
    }.replace("Pc", "PC").replace("Ps", "PS")
