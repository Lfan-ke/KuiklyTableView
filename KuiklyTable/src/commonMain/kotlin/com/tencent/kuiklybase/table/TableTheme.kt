/*
 * Tencent is pleased to support the open source community by making KuiklyUI
 * available.
 * Copyright (C) 2026 Tencent. All rights reserved.
 * Licensed under the License of KuiklyUI;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * https://github.com/Tencent-TDS/KuiklyUI/blob/main/LICENSE
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tencent.kuiklybase.table

import com.tencent.kuikly.core.base.Color
import com.tencent.kuikly.core.base.ViewContainer
import com.tencent.kuikly.core.views.Text
import com.tencent.kuikly.core.views.View

/**
 * Visual theme for Table / HTable.
 *
 * Apply via [TableAttr.theme] to set separator colours, then use
 * [ThemedHeaderRow] / [ThemedTableRow] / [SortableHeaderCell] to apply
 * the same theme to rows and cells.
 */
data class TableTheme(
    val headerBackground: Color = Color(0xFFF5F5F5L),
    val headerTextColor: Color = Color(0xFF333333L),
    val rowBackground: Color = Color(0xFFFFFFFFL),
    val alternateRowBackground: Color = Color(0xFFFAFAFAL),
    val separatorColor: Color = Color(0xFFE0E0E0L),
    val separatorHeight: Float = 0.5f,
    val selectedBackground: Color = Color(0xFFE3F2FDL),
    val rowHeight: Float = 48f,
    val headerRowHeight: Float = 44f,
    val cellPaddingHorizontal: Float = 12f,
) {
    companion object {
        /** Standard light table - matches Material Design / Ant Design defaults. */
        val Default = TableTheme()

        /** High-contrast dark table for dark-mode layouts. */
        val Dark = TableTheme(
            headerBackground = Color(0xFF2D2D2DL),
            headerTextColor = Color(0xFFEEEEEEL),
            rowBackground = Color(0xFF1A1A1AL),
            alternateRowBackground = Color(0xFF222222L),
            separatorColor = Color(0xFF404040L),
            selectedBackground = Color(0xFF1565C0L),
        )

        /** Vivid blue header - inspired by Ant Design brand color. */
        val AntBlue = TableTheme(
            headerBackground = Color(0xFF1677FFL),
            headerTextColor = Color(0xFFFFFFFFL),
            rowBackground = Color(0xFFFFFFFFL),
            alternateRowBackground = Color(0xFFE6F4FFL),
            separatorColor = Color(0xFF91CAFFL),
            selectedBackground = Color(0xFFBAE0FFL),
        )

        /** Teal accent - inspired by TDesign / Material Teal style. */
        val Teal = TableTheme(
            headerBackground = Color(0xFF00897BL),
            headerTextColor = Color(0xFFFFFFFFL),
            rowBackground = Color(0xFFFFFFFFL),
            alternateRowBackground = Color(0xFFE0F2F1L),
            separatorColor = Color(0xFF80CBC4L),
            selectedBackground = Color(0xFFB2DBDBL),
        )
    }
}

/** Sort direction for [SortableHeaderCell]. */
enum class SortOrder { NONE, ASC, DESC }

/**
 * Applies separator color and height from [theme] to this [TableAttr].
 */
fun TableAttr.theme(theme: TableTheme) {
    separatorColor(theme.separatorColor)
    separatorHeight(theme.separatorHeight)
}

/**
 * Themed header row that applies [TableTheme.headerBackground] and [TableTheme.headerRowHeight].
 */
fun ViewContainer<*, *>.ThemedHeaderRow(
    theme: TableTheme = TableTheme.Default,
    init: TableRowView.() -> Unit,
) {
    TableRow {
        attr {
            rowHeight(theme.headerRowHeight)
            backgroundColor(theme.headerBackground)
            flexDirectionRow()
        }
        init()
    }
}

/**
 * Themed data row with even/odd zebra striping from [theme].
 *
 * [index] is 0-based; even indices use [TableTheme.rowBackground],
 * odd indices use [TableTheme.alternateRowBackground].
 */
fun ViewContainer<*, *>.ThemedTableRow(
    theme: TableTheme = TableTheme.Default,
    index: Int,
    init: TableRowView.() -> Unit,
) {
    TableRow {
        attr {
            rowHeight(theme.rowHeight)
            backgroundColor(if (index % 2 == 0) theme.rowBackground else theme.alternateRowBackground)
            flexDirectionRow()
        }
        init()
    }
}

/**
 * Header cell with an optional sort indicator (▲ / ▼).
 *
 * Tapping the cell calls [onSortClick]; the caller manages [SortOrder] state
 * and triggers a rebuild to update the indicator.
 */
fun ViewContainer<*, *>.SortableHeaderCell(
    text: String,
    sortOrder: SortOrder = SortOrder.NONE,
    theme: TableTheme = TableTheme.Default,
    onSortClick: () -> Unit = {},
    init: TableCellView.() -> Unit = {},
) {
    TableCell {
        attr {
            flexDirectionRow()
            alignItemsCenter()
            paddingLeft(theme.cellPaddingHorizontal)
            paddingRight(8f)
        }
        event {
            click { onSortClick() }
        }
        Text {
            attr {
                fontSize(14f)
                color(theme.headerTextColor)
                fontWeight700()
                text(text)
                flex(1f)
            }
        }
        if (sortOrder != SortOrder.NONE) {
            View {
                attr {
                    width(16f)
                    justifyContentCenter()
                    alignItemsCenter()
                }
                Text {
                    attr {
                        fontSize(10f)
                        color(theme.headerTextColor)
                        text(if (sortOrder == SortOrder.ASC) "▲" else "▼")
                    }
                }
            }
        }
        init()
    }
}
