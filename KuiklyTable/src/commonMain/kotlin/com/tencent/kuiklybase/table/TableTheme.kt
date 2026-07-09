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
import com.tencent.kuikly.core.base.ComposeAttr
import com.tencent.kuikly.core.base.ComposeEvent
import com.tencent.kuikly.core.base.ComposeView
import com.tencent.kuikly.core.base.Direction
import com.tencent.kuikly.core.base.Rotate
import com.tencent.kuikly.core.base.Translate
import com.tencent.kuikly.core.base.ViewBuilder
import com.tencent.kuikly.core.base.ViewContainer
import com.tencent.kuikly.core.base.event.PanGestureParams
import com.tencent.kuikly.core.reactive.handler.observable
import com.tencent.kuikly.core.views.Input
import com.tencent.kuikly.core.views.Scroller
import com.tencent.kuikly.core.views.Text
import com.tencent.kuikly.core.views.View
import kotlin.math.abs

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
 * Header cell with an animated sort indicator.
 *
 * The ↑ arrow rotates 180° for DESC via [Rotate] + [Animation.easeInOut],
 * and fades out when [sortOrder] is [SortOrder.NONE].
 * Tapping cycles NONE → ASC → DESC → NONE via [onSortClick].
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
        event { click { onSortClick() } }
        Text {
            attr {
                fontSize(14f)
                color(theme.headerTextColor)
                fontWeightMedium()
                text(text)
                flex(1f)
            }
        }
        val isActive = sortOrder != SortOrder.NONE
        val isDesc = sortOrder == SortOrder.DESC
        View {
            attr {
                size(16f, 16f)
                justifyContentCenter()
                alignItemsCenter()
                opacity(if (isActive) 1f else 0.3f)
                transform(rotate = Rotate(if (isDesc) 180f else 0f, 0f, 0f))
                animate(Animation.easeInOut(0.22f), isDesc)
            }
            Text {
                attr {
                    fontSize(11f)
                    color(if (isActive) theme.headerTextColor else Color(0xFF999999L))
                    fontWeightBold()
                    text("↑")
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
                    animate(Animation.easeOut(0.18f), isActive)
                }
                if (!isActive) event { click { onPageChange(page) } }
                Text {
                    attr {
                        fontSize(13f)
                        fontWeightMedium()
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

/**
 * Search/filter bar above a [Table] - matches Ant Design / Arco Design / TDesign table
 * filter patterns.
 *
 * Uses Kuikly's [Input] view for real text entry. The caller manages [query] state
 * and handles [onQueryChange] to filter the data source, keeping this composable
 * fully stateless.
 *
 * @param query Current search string (empty means no filter).
 * @param theme Table theme for consistent colors.
 * @param placeholder Hint text shown when query is empty.
 * @param onQueryChange Called with the new query on each keystroke.
 * @param onClear Called when the clear (×) button is tapped.
 */
fun ViewContainer<*, *>.TableSearchBar(
    query: String,
    theme: TableTheme = TableTheme.Default,
    placeholder: String = "搜索…",
    onQueryChange: (String) -> Unit = {},
    onClear: () -> Unit = {},
) {
    View {
        attr {
            height(50f)
            flexDirectionRow()
            alignItemsCenter()
            paddingLeft(12f)
            paddingRight(12f)
            paddingTop(6f)
            paddingBottom(6f)
            backgroundColor(theme.rowBackground)
            boxShadow(BoxShadow(0f, 1f, 4f, Color(0x14000000)))
        }
        // Search field container
        View {
            attr {
                flex(1f)
                height(36f)
                flexDirectionRow()
                alignItemsCenter()
                backgroundColor(Color(0xFFF5F5F5L))
                borderRadius(10f)
                paddingLeft(10f)
                paddingRight(10f)
                border(Border(1f, BorderStyle.SOLID,
                    if (query.isNotEmpty()) theme.headerBackground else Color(0xFFE0E0E0L)))
                animate(Animation.easeOut(0.2f), query.isNotEmpty())
            }
            // Magnifier icon
            Text {
                attr {
                    fontSize(13f)
                    color(Color(0xFF999999L))
                    text("🔍")
                    marginRight(6f)
                }
            }
            // Real Input field for text entry
            Input {
                attr {
                    flex(1f)
                    fontSize(14f)
                    color(Color(0xFF212121L))
                    placeholderColor(Color(0xFFBBBBBBL))
                    placeholder(placeholder)
                    text(query)
                    backgroundColor(Color(0x00000000))
                }
                event {
                    textDidChange(isSyncEdit = true) { params ->
                        onQueryChange(params.text)
                    }
                }
            }
        }
        // Clear button - shown only when query is non-empty
        if (query.isNotEmpty()) {
            View {
                attr {
                    size(28f, 28f)
                    marginLeft(8f)
                    borderRadius(14f)
                    backgroundColor(Color(0xFFDDDDDDL))
                    allCenter()
                    animate(Animation.easeInOut(0.15f), query.isNotEmpty())
                }
                event { click { onClear() } }
                Text {
                    attr {
                        fontSize(16f)
                        color(Color(0xFF666666L))
                        fontWeightBold()
                        text("×")
                    }
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

/**
 * Fixed-header table: header stays pinned at top while body rows scroll.
 *
 * Mirrors Element Plus Table's `height` prop and Ant Design's `sticky` header -
 * the header row sits outside the scroller so it never scrolls away.
 *
 * @param height Total height of the component (header + scrollable body).
 * @param theme Visual theme.
 * @param headerContent Cell content for the fixed header row.
 * @param bodyContent Data rows to place inside the scrollable body.
 */
fun ViewContainer<*, *>.StickyTable(
    height: Float = 240f,
    theme: TableTheme = TableTheme.Default,
    headerContent: TableRowView.() -> Unit,
    bodyContent: ViewContainer<*, *>.() -> Unit,
) {
    View {
        attr {
            flexDirectionColumn()
            height(height)
        }
        TableRow {
            attr {
                rowHeight(theme.headerRowHeight)
                backgroundColor(theme.headerBackground)
                flexDirectionRow()
                zIndex(10)
                boxShadow(BoxShadow(0f, 2f, 6f, Color(red255 = 0, green255 = 0, blue255 = 0, alpha01 = 0.12f)))
            }
            headerContent()
        }
        Scroller {
            attr {
                flex(1f)
                flexDirectionColumn()
            }
            bodyContent()
        }
    }
}

/**
 * Table with a frozen (pinned) first column and horizontally scrollable remaining columns.
 *
 * Matches Ant Design/Element Plus `fixed: 'left'` column and TDesign `fixedLeftColumn` behavior.
 * The frozen column casts a subtle right-side shadow to signal the scroll boundary.
 *
 * @param rowCount Number of data rows (used to render frozen cells in sync).
 * @param rowHeight Height per data row.
 * @param frozenWidth Width of the frozen left column.
 * @param theme Visual theme.
 * @param frozenHeaderContent Content for the frozen header cell.
 * @param frozenRowContent Returns content for the frozen cell at each row index.
 * @param scrollHeaderContent Header cells for the scrollable columns.
 * @param scrollRowContent Returns row content for each data row index.
 */
fun ViewContainer<*, *>.FrozenColumnTable(
    rowCount: Int,
    rowHeight: Float = 48f,
    frozenWidth: Float = 80f,
    scrollWidth: Float = 600f,
    theme: TableTheme = TableTheme.Default,
    frozenHeaderContent: TableCellView.() -> Unit,
    frozenRowContent: (Int) -> (TableCellView.() -> Unit),
    scrollHeaderContent: TableRowView.() -> Unit,
    scrollRowContent: (Int) -> (TableRowView.() -> Unit),
) {
    View {
        attr { flexDirectionRow() }

        // Frozen left column - shadow signals scroll boundary
        View {
            attr {
                width(frozenWidth)
                flexDirectionColumn()
                zIndex(5)
                boxShadow(BoxShadow(2f, 0f, 6f, Color(red255 = 0, green255 = 0, blue255 = 0, alpha01 = 0.08f)))
            }
            TableRow {
                attr {
                    rowHeight(theme.headerRowHeight)
                    backgroundColor(theme.headerBackground)
                }
                TableCell { frozenHeaderContent() }
            }
            repeat(rowCount) { idx ->
                TableRow {
                    attr {
                        rowHeight(rowHeight)
                        backgroundColor(if (idx % 2 == 0) theme.rowBackground else theme.alternateRowBackground)
                    }
                    TableCell { frozenRowContent(idx)() }
                }
            }
        }

        // Horizontally scrollable columns
        HTable(tableWidth = scrollWidth) {
            attr { flex(1f) }
            TableRow {
                attr {
                    rowHeight(theme.headerRowHeight)
                    backgroundColor(theme.headerBackground)
                }
                scrollHeaderContent()
            }
            repeat(rowCount) { idx ->
                TableRow {
                    attr {
                        rowHeight(rowHeight)
                        backgroundColor(if (idx % 2 == 0) theme.rowBackground else theme.alternateRowBackground)
                    }
                    scrollRowContent(idx)()
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// SwipeableTableRow - swipe-to-reveal actions (Vant SwipeCell / iOS Mail style)
// ---------------------------------------------------------------------------

/**
 * A single action button shown when a row is swiped.
 *
 * @param label Text label (may include an emoji/icon prefix).
 * @param color Background color of the action button.
 * @param textColor Label color. Defaults to white.
 * @param icon Optional emoji/icon prefix displayed above the label.
 * @param width Button width in dp.
 */
data class SwipeAction(
    val label: String,
    val color: Color,
    val textColor: Color = Color(0xFFFFFFFFL),
    val icon: String = "",
    val width: Float = 72f,
)

class SwipeableTableRowAttr : ComposeAttr() {
    internal var leftActions by observable(emptyList<SwipeAction>())
    internal var rightActions by observable(emptyList<SwipeAction>())
    internal var rowHeight by observable(56f)
    internal var snapThreshold by observable(0.4f)
    internal var theme by observable(TableTheme.Default)
    internal var contentBuilder: (ViewContainer<*, *>.() -> Unit)? = null

    fun leftActions(vararg a: SwipeAction) { leftActions = a.toList() }
    fun rightActions(vararg a: SwipeAction) { rightActions = a.toList() }
    fun rowHeight(h: Float) { rowHeight = h }
    fun snapThreshold(t: Float) { snapThreshold = t }
    fun theme(t: TableTheme) { theme = t }
    fun content(builder: ViewContainer<*, *>.() -> Unit) { contentBuilder = builder }
}

class SwipeableTableRowEvent : ComposeEvent() {
    var onAction: ((side: String, index: Int) -> Unit)? = null
    fun onAction(block: (side: String, index: Int) -> Unit) { onAction = block }
}

class SwipeableTableRowView : ComposeView<SwipeableTableRowAttr, SwipeableTableRowEvent>() {

    // Current content translation: negative = swiped left (right actions visible),
    // positive = swiped right (left actions visible).
    private var offsetX by observable(0f)

    // Gesture bookkeeping - not reactive, updated synchronously during pan.
    private var panStartPageX = 0f
    private var panStartOffset = 0f

    override fun createAttr(): SwipeableTableRowAttr = SwipeableTableRowAttr()
    override fun createEvent(): SwipeableTableRowEvent = SwipeableTableRowEvent()

    override fun body(): ViewBuilder {
        val ctx = this
        return {
            // TableRow wrapper - keeps this ComposeView transparent (no attrs on `this`)
            // so the flat-layer mechanism lets it integrate seamlessly into a Table.
            TableRow {
                attr {
                    rowHeight(ctx.attr.rowHeight)
                    backgroundColor(ctx.attr.theme.rowBackground)
                }

                val leftActions = ctx.attr.leftActions
                val rightActions = ctx.attr.rightActions
                val leftWidth = leftActions.sumOf { it.width.toDouble() }.toFloat()
                val rightWidth = rightActions.sumOf { it.width.toDouble() }.toFloat()

                // Clip container fills the row and hides action buttons that are off-screen.
                View {
                    attr {
                        flex(1f)
                        overflow(true)
                    }

                    // Left action buttons - sit at the left edge, revealed when content shifts right.
                    if (leftActions.isNotEmpty()) {
                        View {
                            attr {
                                absolutePosition(top = 0f, left = 0f, bottom = 0f)
                                width(leftWidth)
                                flexDirectionRow()
                            }
                            leftActions.forEachIndexed { index, action ->
                                ctx.actionButton(this, action, "left", index)
                            }
                        }
                    }

                    // Right action buttons - sit at the right edge, revealed when content shifts left.
                    if (rightActions.isNotEmpty()) {
                        View {
                            attr {
                                absolutePosition(top = 0f, right = 0f, bottom = 0f)
                                width(rightWidth)
                                flexDirectionRow()
                            }
                            rightActions.forEachIndexed { index, action ->
                                ctx.actionButton(this, action, "right", index)
                            }
                        }
                    }

                    // Content layer - slides over the action buttons via offsetX translation.
                    View {
                        attr {
                            absolutePositionAllZero()
                            backgroundColor(ctx.attr.theme.rowBackground)
                            transform(Translate(0f, 0f, ctx.offsetX, 0f))
                            animate(Animation.easeOut(0.25f), ctx.offsetX)
                        }
                        event {
                            pan { params -> ctx.handlePan(params, leftWidth, rightWidth) }
                            click { if (ctx.offsetX != 0f) ctx.snapClose() }
                        }
                        ctx.attr.contentBuilder?.invoke(this)
                    }
                }
            }
        }
    }

    // Renders a single action button into [container].
    private fun actionButton(
        container: ViewContainer<*, *>,
        action: SwipeAction,
        side: String,
        index: Int,
    ) {
        val ctx = this
        container.View {
            attr {
                width(action.width)
                flex(1f)
                allCenter()
                flexDirectionColumn()
                backgroundColor(action.color)
                val shadowX = if (side == "left") 1f else -1f
                boxShadow(BoxShadow(shadowX, 0f, 4f, Color(red255 = 0, green255 = 0, blue255 = 0, alpha01 = 0.15f)))
            }
            event {
                click {
                    ctx.event.onAction?.invoke(side, index)
                    ctx.snapClose()
                }
            }
            if (action.icon.isNotEmpty()) {
                Text {
                    attr {
                        fontSize(18f)
                        text(action.icon)
                        marginBottom(2f)
                    }
                }
            }
            Text {
                attr {
                    fontSize(12f)
                    color(action.textColor)
                    fontWeightMedium()
                    text(action.label)
                }
            }
        }
    }

    private fun handlePan(params: PanGestureParams, leftWidth: Float, rightWidth: Float) {
        when (params.state) {
            "start" -> {
                panStartPageX = params.pageX
                panStartOffset = offsetX
            }
            "move" -> {
                val delta = params.pageX - panStartPageX
                val raw = panStartOffset + delta
                val maxRight = if (attr.leftActions.isNotEmpty()) leftWidth * 1.1f else 0f
                val maxLeft = if (attr.rightActions.isNotEmpty()) -rightWidth * 1.1f else 0f
                offsetX = raw.coerceIn(maxLeft, maxRight)
            }
            "end" -> {
                val threshold = attr.snapThreshold
                when {
                    offsetX < 0f && abs(offsetX) >= rightWidth * threshold -> snapOpen(-rightWidth)
                    offsetX > 0f && abs(offsetX) >= leftWidth * threshold -> snapOpen(leftWidth)
                    else -> snapClose()
                }
            }
        }
    }

    private fun snapOpen(target: Float) { offsetX = target }
    private fun snapClose() { offsetX = 0f }
}

/**
 * A table row that reveals swipeable action buttons when the user pans horizontally -
 * matching the Vant SwipeCell, iOS Mail, and Ant Design Mobile SwipeAction pattern.
 */
fun ViewContainer<*, *>.SwipeableTableRow(init: SwipeableTableRowView.() -> Unit) {
    addChild(SwipeableTableRowView(), init)
}

// =============================================================================
// TreeTable - hierarchical table with expand/collapse (Element Plus / Ant Design)
// =============================================================================

/**
 * A single node in the tree table.
 *
 * @param id       Unique string identifier used to track expand state.
 * @param cells    Cell text values, one per column.
 * @param children Nested child rows; empty list = leaf node.
 * @param tag      Optional caller-supplied data object for event callbacks.
 */
data class TreeTableNode(
    val id: String,
    val cells: List<String>,
    val children: List<TreeTableNode> = emptyList(),
    val tag: Any? = null,
)

/** A single column definition for [TreeTableView]. */
data class TreeTableColumn(
    val header: String,
    val flex: Float = 1f,
    val align: String = "left",  // "left", "center", "right"
)

class TreeTableAttr : ComposeAttr() {

    internal var nodes by observable(emptyList<TreeTableNode>())
    internal var columns by observable(emptyList<TreeTableColumn>())
    internal var expandedIds by observable(emptySet<String>())
    internal var defaultExpandAll by observable(false)
    internal var rowHeight by observable(44f)
    internal var indentWidth by observable(20f)
    internal var showExpandIcon by observable(true)

    // colors
    internal var headerBackground by observable(Color(0xFFF5F5F5L))
    internal var headerTextColor by observable(Color(0xFF333333L))
    internal var rowBackground by observable(Color(0xFFFFFFFFL))
    internal var rowAltBackground by observable(Color(0xFFFAFAFAL))
    internal var rowTextColor by observable(Color(0xFF555555L))
    internal var separatorColor by observable(Color(0xFFEEEEEEL))
    internal var expandIconColor by observable(Color(0xFF1677FFL))
    internal var headerFontSize by observable(13f)
    internal var rowFontSize by observable(13f)

    fun nodes(list: List<TreeTableNode>) { nodes = list }
    fun nodes(vararg n: TreeTableNode) { nodes = n.toList() }
    fun columns(list: List<TreeTableColumn>) { columns = list }
    fun columns(vararg c: TreeTableColumn) { columns = c.toList() }
    fun expandedIds(ids: Set<String>) { expandedIds = ids }
    fun defaultExpandAll(b: Boolean) { defaultExpandAll = b }
    fun rowHeight(h: Float) { rowHeight = h.coerceAtLeast(28f) }
    fun indentWidth(w: Float) { indentWidth = w.coerceAtLeast(8f) }
    fun showExpandIcon(show: Boolean) { showExpandIcon = show }
    fun headerBackground(c: Color) { headerBackground = c }
    fun rowBackground(c: Color) { rowBackground = c }
    fun rowTextColor(c: Color) { rowTextColor = c }
    fun separatorColor(c: Color) { separatorColor = c }
    fun theme(t: TableTheme) {
        headerBackground = t.headerBackground
        headerTextColor = t.headerTextColor
        rowBackground = t.rowBackground
        separatorColor = t.separatorColor
    }
}

class TreeTableEvent : ComposeEvent() {
    var onExpandChange: ((nodeId: String, expanded: Boolean) -> Unit)? = null
    var onRowClick: ((node: TreeTableNode, depth: Int) -> Unit)? = null
}

class TreeTableView : ComposeView<TreeTableAttr, TreeTableEvent>() {

    // Mutable set of expanded IDs tracked as observable string (hash-join hack)
    private var expandedSnapshot by observable("")

    private val localExpanded = mutableSetOf<String>()

    override fun createAttr(): TreeTableAttr = TreeTableAttr()
    override fun createEvent(): TreeTableEvent = TreeTableEvent()

    override fun didInit() {
        super.didInit()
        if (attr.defaultExpandAll) {
            collectAllIds(attr.nodes).forEach { localExpanded.add(it) }
        } else {
            localExpanded.addAll(attr.expandedIds)
        }
        snapExpanded()
    }

    private fun snapExpanded() { expandedSnapshot = localExpanded.joinToString(",") }

    private fun collectAllIds(nodes: List<TreeTableNode>): List<String> =
        nodes.flatMap { listOf(it.id) + collectAllIds(it.children) }

    private fun toggle(id: String) {
        if (localExpanded.contains(id)) localExpanded.remove(id) else localExpanded.add(id)
        snapExpanded()
        event.onExpandChange?.invoke(id, localExpanded.contains(id))
    }

    override fun body(): ViewBuilder {
        val ctx = this
        return {
            // Force re-render on expandedSnapshot change (reactive dependency)
            @Suppress("UNUSED_VARIABLE")
            val expandedKey = ctx.expandedSnapshot

            View {
                attr { flexDirectionColumn() }

                // Header row
                View {
                    attr {
                        flexDirectionRow()
                        height(ctx.attr.rowHeight)
                        backgroundColor(ctx.attr.headerBackground)
                        alignItems(com.tencent.kuikly.core.layout.FlexAlign.CENTER)
                    }
                    ctx.attr.columns.forEach { col ->
                        View {
                            attr {
                                flex(col.flex)
                                height(ctx.attr.rowHeight)
                                allCenter()
                                paddingLeft(8f)
                                paddingRight(8f)
                            }
                            Text {
                                attr {
                                    text(col.header)
                                    fontSize(ctx.attr.headerFontSize)
                                    fontWeightMedium()
                                    color(ctx.attr.headerTextColor)
                                }
                            }
                        }
                    }
                }
                View { attr { height(0.5f); backgroundColor(ctx.attr.separatorColor) } }

                // Flattened visible rows
                ctx.renderNodes(this, ctx.attr.nodes, depth = 0, altBase = 0)
            }
        }
    }

    private fun renderNodes(
        parent: ViewContainer<*, *>,
        nodes: List<TreeTableNode>,
        depth: Int,
        altBase: Int,
    ) {
        val ctx = this
        var rowIndex = altBase
        nodes.forEach { node ->
            val isExpanded = ctx.localExpanded.contains(node.id)
            val hasChildren = node.children.isNotEmpty()
            val finalDepth = depth
            val finalIsExpanded = isExpanded
            val finalNode = node

            parent.View {
                attr {
                    flexDirectionColumn()
                }

                // Row itself
                View {
                    attr {
                        flexDirectionRow()
                        alignItems(com.tencent.kuikly.core.layout.FlexAlign.CENTER)
                        height(ctx.attr.rowHeight)
                        backgroundColor(if (rowIndex % 2 == 0) ctx.attr.rowBackground else ctx.attr.rowAltBackground)
                    }
                    event { click { ctx.event.onRowClick?.invoke(finalNode, finalDepth) } }

                    // First cell with indent + expand icon
                    View {
                        attr {
                            flex(ctx.attr.columns.firstOrNull()?.flex ?: 1f)
                            flexDirectionRow()
                            alignItems(com.tencent.kuikly.core.layout.FlexAlign.CENTER)
                            paddingLeft(8f + finalDepth * ctx.attr.indentWidth)
                            paddingRight(4f)
                        }

                        if (ctx.attr.showExpandIcon) {
                            if (hasChildren) {
                                Text {
                                    attr {
                                        text(if (finalIsExpanded) "▾" else "▸")
                                        fontSize(12f)
                                        color(ctx.attr.expandIconColor)
                                        marginRight(6f)
                                        animate(Animation.easeInOut(0.15f), finalIsExpanded)
                                    }
                                    event { click { ctx.toggle(finalNode.id) } }
                                }
                            } else {
                                // leaf indent placeholder
                                View { attr { width(18f) } }
                            }
                        }

                        Text {
                            attr {
                                text(node.cells.getOrElse(0) { "" })
                                fontSize(ctx.attr.rowFontSize)
                                color(ctx.attr.rowTextColor)
                                flex(1f)
                            }
                        }
                    }

                    // Remaining cells
                    for (colIdx in 1 until ctx.attr.columns.size) {
                        val col = ctx.attr.columns[colIdx]
                        View {
                            attr { flex(col.flex); allCenter(); paddingLeft(4f); paddingRight(4f) }
                            Text {
                                attr {
                                    text(node.cells.getOrElse(colIdx) { "" })
                                    fontSize(ctx.attr.rowFontSize)
                                    color(ctx.attr.rowTextColor)
                                }
                            }
                        }
                    }
                }
                View { attr { height(0.5f); backgroundColor(ctx.attr.separatorColor) } }

                // Children (only if expanded)
                if (isExpanded && hasChildren) {
                    ctx.renderNodes(this, node.children, depth + 1, rowIndex + 1)
                }
            }
            rowIndex++
        }
    }
}

fun ViewContainer<*, *>.TreeTable(init: TreeTableView.() -> Unit) {
    addChild(TreeTableView(), init)
}

// ---------------------------------------------------------------------------
// InfiniteTable - load-more / infinite-scroll variant
// ---------------------------------------------------------------------------

/**
 * Table variant with automatic infinite-scroll / load-more footer.
 *
 * Identical to [Table] for row content; appends a footer row that reflects
 * [isLoading] / [hasMore] state and fires [onLoadMore] via native
 * [TableEvent.reachEnd] or when the user taps the footer manually.
 */
fun ViewContainer<*, *>.InfiniteTable(
    isLoading: Boolean,
    hasMore: Boolean,
    onLoadMore: () -> Unit,
    theme: TableTheme = TableTheme.Default,
    height: Float = 360f,
    init: TableView.() -> Unit,
) {
    Table {
        attr {
            this.height(height)
            separatorColor(theme.separatorColor)
            separatorHeight(theme.separatorHeight)
        }
        event {
            reachEnd {
                if (!isLoading && hasMore) onLoadMore()
            }
        }
        this.init()
        // Footer row - acts as load-more trigger and status indicator
        TableRow {
            attr {
                rowHeight(48f)
                backgroundColor(Color(0xFFFAFAFAL))
                flexDirectionRow()
            }
            event {
                rowClick { _ -> if (!isLoading && hasMore) onLoadMore() }
            }
            TableCell {
                attr {
                    flex(1f)
                    allCenter()
                }
                if (isLoading) {
                    View {
                        attr {
                            flexDirectionRow()
                            allCenter()
                        }
                        View {
                            attr {
                                width(14f)
                                height(14f)
                                borderRadius(7f)
                                border(Border(2f, BorderStyle.SOLID, Color(0xFF1677FFL)))
                                marginRight(8f)
                            }
                        }
                        Text {
                            attr {
                                fontSize(13f)
                                color(Color(0xFF1677FFL))
                                text("加载中...")
                            }
                        }
                    }
                } else {
                    Text {
                        attr {
                            fontSize(13f)
                            color(if (hasMore) Color(0xFF1677FFL) else Color(0xFFBBBBBBL))
                            text(if (hasMore) "点击或上拉加载更多" else "全部加载完毕")
                        }
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// EditableTableRow - inline cell editing (Ant Design editable table pattern)
// ---------------------------------------------------------------------------

/**
 * A single editable field descriptor.
 *
 * @param value  Current display value
 * @param editable  Whether this cell allows editing
 * @param placeholder  Input placeholder when empty
 * @param onChange  Called whenever the text changes
 */
data class EditableField(
    val value: String,
    val editable: Boolean = true,
    val placeholder: String = "",
    val onChange: ((String) -> Unit)? = null,
)

/**
 * Table row where each cell is either a plain label or an [Input] field,
 * toggled by [editing]. Matches Ant Design / Element Plus editable-table UX:
 * display mode shows the value as text, edit mode reveals an inline input.
 */
fun ViewContainer<*, *>.EditableTableRow(
    fields: List<EditableField>,
    editing: Boolean,
    theme: TableTheme = TableTheme.Default,
    index: Int = 0,
    actionContent: (ViewContainer<*, *>.() -> Unit)? = null,
) {
    TableRow {
        attr {
            rowHeight(if (editing) 52f else 44f)
            flexDirectionRow()
            backgroundColor(
                if (editing) Color(0xFFFAFBFFL)
                else if (index % 2 == 0) theme.rowBackground else theme.alternateRowBackground
            )
        }
        fields.forEach { field ->
            TableCell {
                attr {
                    flex(1f)
                    justifyContentCenter()
                    paddingLeft(8f)
                    paddingRight(8f)
                    paddingTop(4f)
                    paddingBottom(4f)
                }
                if (editing && field.editable) {
                    View {
                        attr {
                            flex(1f)
                            height(36f)
                            border(Border(1f, BorderStyle.SOLID, Color(0xFF1677FFL)))
                            borderRadius(6f)
                            paddingLeft(8f)
                            paddingRight(8f)
                            justifyContentCenter()
                            backgroundColor(Color(0xFFFFFFFFL))
                        }
                        Input {
                            attr {
                                flex(1f)
                                fontSize(14f)
                                color(Color(0xFF212121L))
                                value(field.value)
                                placeholder(field.placeholder)
                                placeholderColor(Color(0xFFBBBBBBL))
                            }
                            event {
                                textChange { text -> field.onChange?.invoke(text) }
                            }
                        }
                    }
                } else {
                    Text {
                        attr {
                            fontSize(14f)
                            color(if (field.value.isEmpty()) Color(0xFFBBBBBBL) else Color(0xFF212121L))
                            text(field.value.ifEmpty { field.placeholder })
                            flex(1f)
                        }
                    }
                }
            }
        }
        if (actionContent != null) {
            TableCell {
                attr {
                    width(90f)
                    justifyContentCenter()
                    alignItemsCenter()
                    flexDirectionRow()
                    paddingLeft(4f)
                    paddingRight(4f)
                }
                actionContent()
            }
        }
    }
}
