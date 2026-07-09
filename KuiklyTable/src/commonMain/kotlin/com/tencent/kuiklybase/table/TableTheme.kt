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

import com.tencent.kuikly.core.base.Animation
import com.tencent.kuikly.core.base.Border
import com.tencent.kuikly.core.base.BorderStyle
import com.tencent.kuikly.core.base.BoxShadow
import com.tencent.kuikly.core.base.Color
import com.tencent.kuikly.core.base.ColorStop
import com.tencent.kuikly.core.base.Direction
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
 * Empty-state placeholder shown when a table has no data rows.
 * Renders a centered "暂无数据" label (customizable via [message]) styled
 * to the active [theme].
 *
 * Place this inside the [Table] DSL block, after the header row, when
 * your data list is empty.
 *
 * Example:
 * ```kotlin
 * Table {
 *     ThemedHeaderRow(theme) { /* … */ }
 *     if (items.isEmpty()) {
 *         TableEmptyView(theme)
 *     } else {
 *         items.forEachIndexed { i, item -> ThemedTableRow(theme, i) { /* … */ } }
 *     }
 * }
 * ```
 */
fun ViewContainer<*, *>.TableEmptyView(
    theme: TableTheme = TableTheme.Default,
    height: Float = 120f,
    message: String = "暂无数据",
) {
    TableRow {
        attr {
            rowHeight(height)
            backgroundColor(theme.rowBackground)
            flexDirectionRow()
            justifyContentCenter()
            alignItemsCenter()
        }
        Text {
            attr {
                fontSize(14f)
                color(Color(0xFFBBBBBBL))
                text(message)
            }
        }
    }
}

/**
 * Themed summary (footer) row pinned below data rows — matches the
 * Element Plus and Arco Design "summary row" pattern.
 *
 * Applies [TableTheme.headerBackground] at reduced opacity via a slightly
 * lighter tint, and [TableTheme.headerRowHeight] for height.
 * The [init] block receives a [TableRowView] so you can add [TableCell]
 * children with aggregated values (sum, average, etc.).
 *
 * @param theme Active table theme.
 * @param init  Row content builder.
 */
fun ViewContainer<*, *>.ThemedSummaryRow(
    theme: TableTheme = TableTheme.Default,
    init: TableRowView.() -> Unit,
) {
    TableRow {
        attr {
            rowHeight(theme.headerRowHeight)
            backgroundColor(theme.alternateRowBackground)
            flexDirectionRow()
        }
        init()
    }
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

/**
 * Themed pagination bar for use below a [Table] or [HTable].
 *
 * The parent page holds [currentPage] state and handles [onPageChange] to
 * update it; this composable is stateless and re-renders on each state change.
 *
 * @param currentPage 1-based current page index.
 * @param totalPages Total number of pages. Computed as `ceil(totalItems / pageSize)`.
 * @param theme Visual theme for colors. Defaults to [TableTheme.Default].
 * @param showTotal When true, shows "共 N 条" label if [totalItems] > 0.
 * @param totalItems Total item count displayed alongside the pagination.
 * @param onPageChange Called with the new 1-based page number when user taps a page button.
 */
fun ViewContainer<*, *>.PaginationBar(
    currentPage: Int,
    totalPages: Int,
    theme: TableTheme = TableTheme.Default,
    showTotal: Boolean = true,
    totalItems: Int = 0,
    onPageChange: (Int) -> Unit = {},
) {
    if (totalPages <= 1 && totalItems == 0) return
    View {
        attr {
            height(48f)
            flexDirectionRow()
            alignItemsCenter()
            justifyContentCenter()
            backgroundColor(theme.rowBackground)
        }

        if (showTotal && totalItems > 0) {
            Text {
                attr {
                    fontSize(12f)
                    color(Color(0xFF999999L))
                    text("共 $totalItems 条")
                    marginRight(10f)
                }
            }
        }

        // Previous button
        val hasPrev = currentPage > 1
        View {
            attr {
                width(30f)
                height(30f)
                borderRadius(4f)
                justifyContentCenter()
                alignItemsCenter()
                marginRight(4f)
                backgroundColor(if (hasPrev) theme.rowBackground else theme.alternateRowBackground)
            }
            if (hasPrev) event { click { onPageChange(currentPage - 1) } }
            Text {
                attr {
                    fontSize(16f)
                    color(if (hasPrev) theme.headerBackground else Color(0xFFBBBBBBL))
                    text("‹")
                }
            }
        }

        // Page number buttons (up to 5 around current page)
        val windowStart = maxOf(1, minOf(currentPage - 2, totalPages - 4))
        val windowEnd = minOf(totalPages, windowStart + 4)
        for (page in windowStart..windowEnd) {
            val isActive = page == currentPage
            View {
                attr {
                    width(30f)
                    height(30f)
                    borderRadius(4f)
                    justifyContentCenter()
                    alignItemsCenter()
                    marginRight(4f)
                    backgroundColor(if (isActive) theme.headerBackground else theme.rowBackground)
                }
                if (!isActive) event { click { onPageChange(page) } }
                Text {
                    attr {
                        fontSize(13f)
                        color(if (isActive) theme.headerTextColor else Color(0xFF333333L))
                        text("$page")
                    }
                }
            }
        }

        // Next button
        val hasNext = currentPage < totalPages
        View {
            attr {
                width(30f)
                height(30f)
                borderRadius(4f)
                justifyContentCenter()
                alignItemsCenter()
                backgroundColor(if (hasNext) theme.rowBackground else theme.alternateRowBackground)
            }
            if (hasNext) event { click { onPageChange(currentPage + 1) } }
            Text {
                attr {
                    fontSize(16f)
                    color(if (hasNext) theme.headerBackground else Color(0xFFBBBBBBL))
                    text("›")
                }
            }
        }
    }
}

fun ViewContainer<*, *>.TableHeaderGroup(
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

fun ViewContainer<*, *>.TableGroupCell(
    label: String,
    theme: TableTheme = TableTheme.Default,
    init: TableCellView.() -> Unit = {},
) {
    TableCell {
        attr {
            flexDirectionColumn()
            alignItemsCenter()
            justifyContentCenter()
        }
        Text {
            attr {
                fontSize(13f)
                color(theme.headerTextColor)
                fontWeight700()
                text(label)
            }
        }
        init()
    }
}

fun ViewContainer<*, *>.ExpandableTableRow(
    theme: TableTheme = TableTheme.Default,
    index: Int,
    expanded: Boolean,
    onToggle: () -> Unit = {},
    expandedContent: (ViewContainer<*, *>.() -> Unit)? = null,
    init: TableRowView.() -> Unit,
) {
    TableRow {
        attr {
            rowHeight(theme.rowHeight)
            backgroundColor(if (index % 2 == 0) theme.rowBackground else theme.alternateRowBackground)
            flexDirectionRow()
            alignItemsCenter()
        }
        TableCell {
            attr {
                width(36f)
                justifyContentCenter()
                alignItemsCenter()
            }
            event { click { onToggle() } }
            Text {
                attr {
                    fontSize(12f)
                    color(theme.headerBackground)
                    text(if (expanded) "▼" else "▶")
                }
            }
        }
        init()
    }
    if (expanded && expandedContent != null) {
        TableRow {
            attr {
                backgroundColor(theme.selectedBackground)
                flexDirectionColumn()
                paddingLeft(36f)
                paddingRight(12f)
                paddingTop(8f)
                paddingBottom(8f)
            }
            expandedContent()
        }
    }
}

/**
 * Selectable data row with a leading checkbox indicator column.
 *
 * The caller holds [selected] state and flips it in [onToggle], matching the
 * stateless DSL pattern used throughout this library.
 * Use [TableBatchActionBar] above the table to show batch operations when
 * [selectedCount] > 0.
 *
 * @param theme Visual theme.
 * @param index 0-based row index for zebra striping.
 * @param selected Whether this row is currently checked.
 * @param onToggle Called when the user taps the checkbox column.
 * @param init Main row content (add [TableCell] children here).
 */
fun ViewContainer<*, *>.CheckboxTableRow(
    theme: TableTheme = TableTheme.Default,
    index: Int,
    selected: Boolean,
    onToggle: () -> Unit = {},
    init: TableRowView.() -> Unit,
) {
    TableRow {
        attr {
            rowHeight(theme.rowHeight)
            backgroundColor(
                if (selected) theme.selectedBackground
                else if (index % 2 == 0) theme.rowBackground
                else theme.alternateRowBackground
            )
            flexDirectionRow()
            alignItemsCenter()
        }
        TableCell {
            attr {
                width(40f)
                allCenter()
            }
            event { click { onToggle() } }
            View {
                attr {
                    width(18f)
                    height(18f)
                    borderRadius(3f)
                    allCenter()
                    animate(Animation.easeInOut(0.18f), selected)
                    backgroundColor(if (selected) theme.headerBackground else Color(0xFFFFFFFFL))
                    border(Border(1.5f, BorderStyle.SOLID, if (selected) theme.headerBackground else Color(0xFFCCCCCCL)))
                }
                if (selected) {
                    Text {
                        attr {
                            fontSize(11f)
                            color(theme.headerTextColor)
                            text("✓")
                        }
                    }
                }
            }
        }
        init()
    }
}

/**
 * Action toolbar displayed when one or more rows are selected — matches the
 * Ant Design / Element Plus batch-operation bar pattern.
 *
 * Renders nothing when [selectedCount] is 0 and [actions] is empty.
 *
 * @param selectedCount Number of currently selected rows.
 * @param totalCount Total row count shown in the label when > 0.
 * @param theme Visual theme for colors.
 * @param onSelectAll Called when user taps "全选". Pass null to hide the button.
 * @param onClearAll Called when user taps "取消". Pass null to hide the button.
 * @param actions Custom action buttons as label/onClick pairs (e.g. "删除").
 */
fun ViewContainer<*, *>.TableBatchActionBar(
    selectedCount: Int,
    totalCount: Int = 0,
    theme: TableTheme = TableTheme.Default,
    onSelectAll: (() -> Unit)? = null,
    onClearAll: (() -> Unit)? = null,
    actions: List<Pair<String, () -> Unit>> = emptyList(),
) {
    if (selectedCount == 0 && actions.isEmpty()) return
    View {
        attr {
            height(44f)
            flexDirectionRow()
            alignItemsCenter()
            paddingLeft(12f)
            paddingRight(12f)
            backgroundLinearGradient(
                Direction.TO_RIGHT,
                ColorStop(theme.selectedBackground, 0f),
                ColorStop(Color(red255 = 235, green255 = 245, blue255 = 255, alpha01 = 1f), 1f),
            )
            boxShadow(BoxShadow(0f, 2f, 8f, Color(red255 = 0, green255 = 0, blue255 = 0, alpha01 = 0.12f)))
        }
        Text {
            attr {
                fontSize(13f)
                fontWeightSemiBold()
                color(theme.headerBackground)
                text("已选 $selectedCount${if (totalCount > 0) "/$totalCount" else ""} 项")
                flex(1f)
            }
        }
        if (onSelectAll != null) {
            View {
                attr {
                    height(28f)
                    paddingLeft(10f)
                    paddingRight(10f)
                    borderRadius(4f)
                    allCenter()
                    marginRight(8f)
                    backgroundColor(theme.headerBackground)
                }
                event { click { onSelectAll() } }
                Text {
                    attr {
                        fontSize(12f)
                        color(theme.headerTextColor)
                        text("全选")
                    }
                }
            }
        }
        if (onClearAll != null) {
            View {
                attr {
                    height(28f)
                    paddingLeft(10f)
                    paddingRight(10f)
                    borderRadius(4f)
                    allCenter()
                    marginRight(8f)
                    backgroundColor(Color(0xFFFFFFFFL))
                    border(Border(1f, BorderStyle.SOLID, theme.separatorColor))
                }
                event { click { onClearAll() } }
                Text {
                    attr {
                        fontSize(12f)
                        color(theme.headerBackground)
                        text("取消")
                    }
                }
            }
        }
        actions.forEach { (label, onClick) ->
            View {
                attr {
                    height(28f)
                    paddingLeft(10f)
                    paddingRight(10f)
                    borderRadius(4f)
                    allCenter()
                    marginRight(6f)
                    backgroundColor(Color(0xFFF44336L))
                }
                event { click { onClick() } }
                Text {
                    attr {
                        fontSize(12f)
                        color(Color(0xFFFFFFFFL))
                        text(label)
                    }
                }
            }
        }
    }
}
