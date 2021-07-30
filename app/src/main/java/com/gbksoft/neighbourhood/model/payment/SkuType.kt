package com.gbksoft.neighbourhood.model.payment

enum class SkuType(val sku: String, val productId: String, val radiusValue: Int) {

    SMALL_AREA("sub_area_small", "r100", 100),
    MEDIUM_AREA("sub_area_medium", "r500", 500),
    LARGE_AREA("sub_area_unlimited", "worldwide", 12430);

    companion object {
        fun getSkuType(sku: String) = values().firstOrNull { it.sku == sku }

        fun getSkuTypeByProductName(productId: String) = values().firstOrNull { it.productId == productId }

    }


}