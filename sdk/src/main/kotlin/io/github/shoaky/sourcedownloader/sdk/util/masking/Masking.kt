package io.github.shoaky.sourcedownloader.sdk.util.masking

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import io.github.shoaky.sourcedownloader.sdk.util.masking.MaskingMiddle
import io.github.shoaky.sourcedownloader.sdk.util.masking.StringMaskingSerializer
import kotlin.reflect.KClass

/**
 * jackson字符串脱敏 具体逻辑由[StringMasking]的实现决定
 */
@JacksonAnnotationsInside
@Target(AnnotationTarget.FIELD)
@JsonSerialize(using = StringMaskingSerializer::class)
@Retention(AnnotationRetention.RUNTIME)
annotation class Masking(
    val value: KClass<out StringMasking> = MaskingMiddle::class
)
