package com.tarantino.xonarx.presentation.browser

sealed class ContextualActionTarget {
    data class Link(val url: String) : ContextualActionTarget()
    data class Image(val imageUrl: String) : ContextualActionTarget()
    data class ImageLink(val url: String, val imageUrl: String) : ContextualActionTarget()
    data class Text(val text: String) : ContextualActionTarget()
}
