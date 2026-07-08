package com.shopverse.cmp.network.service.util

/** Thrown by the auth plugin after a silent token refresh so [safeApiCall] retries the request. */
class TokenRefreshedException : RuntimeException()
