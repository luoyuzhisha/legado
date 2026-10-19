package io.legado.app.utils

import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import io.legado.app.R

/**
 * Android 15 对 targetSdk 35 强制 edge-to-edge，系统不会再帮内容避开系统栏。
 *
 * 大部分页面已经由 TitleBar / applyNavigationBar* / 自定义 listener 处理了 inset；
 * 这里只做兜底：没有处理顶部或底部的页面，在根布局上补一次 padding。
 */
fun View.markTopHandled() {
    setTag(R.id.tag_edge_to_edge_top, true)
}

fun View.markBottomHandled() {
    setTag(R.id.tag_edge_to_edge_bottom, true)
}

object EdgeToEdgeInsets {

    fun install(root: View) {
        if (root.getTag(R.id.tag_edge_to_edge_applied) == true) {
            return
        }
        root.setTag(R.id.tag_edge_to_edge_applied, true)
        installWhenInsetsReady(root, 0)
    }

    private fun installWhenInsetsReady(root: View, attempt: Int) {
        root.post {
            if (!root.isAttachedToWindow) {
                return@post
            }
            val insets = ViewCompat.getRootWindowInsets(root)
            if (insets != null) {
                applyFallback(root, insets)
            } else if (attempt < 5) {
                installWhenInsetsReady(root, attempt + 1)
            }
        }
    }

    private fun applyFallback(root: View, insets: WindowInsetsCompat) {
        val bars = insets.getInsets(
            WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
        )
        val top = if (hasTag(root, R.id.tag_edge_to_edge_top)) 0 else bars.top
        val bottom = if (hasTag(root, R.id.tag_edge_to_edge_bottom)) 0 else bars.bottom
        if (top == 0 && bottom == 0) {
            return
        }
        root.updatePadding(
            top = root.paddingTop + top,
            bottom = root.paddingBottom + bottom
        )
    }

    private fun hasTag(view: View, tagId: Int): Boolean {
        if (view.getTag(tagId) == true) {
            return true
        }
        if (view is ViewGroup) {
            for (index in 0 until view.childCount) {
                if (hasTag(view.getChildAt(index), tagId)) {
                    return true
                }
            }
        }
        return false
    }
}
