package com.kuikly.kuiklytable

import com.tencent.kuikly.core.annotations.Page
import com.tencent.kuikly.core.base.Color
import com.tencent.kuikly.core.base.ViewBuilder
import com.tencent.kuikly.core.base.ViewContainer
import com.tencent.kuikly.core.log.KLog
import com.tencent.kuikly.core.reactive.handler.observable
import com.tencent.kuikly.core.views.Text
import com.tencent.kuikly.core.views.View
import com.tencent.kuiklybase.table.HTable
import com.tencent.kuiklybase.table.SortOrder
import com.tencent.kuiklybase.table.SortableHeaderCell
import com.tencent.kuiklybase.table.Table
import com.tencent.kuiklybase.table.TableCell
import com.tencent.kuiklybase.table.TableRow
import com.tencent.kuiklybase.table.TableTheme
import com.tencent.kuiklybase.table.ThemedHeaderRow
import com.tencent.kuiklybase.table.PaginationBar
import com.tencent.kuiklybase.table.TableEmptyView
import com.tencent.kuiklybase.table.ThemedSummaryRow
import com.tencent.kuiklybase.table.ThemedTableRow
import com.tencent.kuiklybase.table.theme
import com.tencent.kuiklybase.table.TableHeaderGroup
import com.tencent.kuiklybase.table.TableGroupCell
import com.tencent.kuiklybase.table.ExpandableTableRow
import com.tencent.kuiklybase.table.CheckboxTableRow
import com.tencent.kuiklybase.table.TableBatchActionBar
import com.tencent.kuiklybase.table.FrozenColumnTable
import com.tencent.kuiklybase.table.StickyTable
import com.tencent.kuiklybase.table.TableSearchBar
import com.tencent.kuiklybase.table.SwipeAction
import com.tencent.kuiklybase.table.SwipeableTableRow
import com.kuikly.kuiklytable.base.BasePager

private data class TableItem(val name: String, val score: Int, val grade: String)
private data class WideItem(val name: String, val dept: String, val score: String, val grade: String, val note: String)

@Page("TableViewDemoPage")
internal class TableViewDemoPage : BasePager() {

    // --- state ---
    private var scoreSortOrder by observable(SortOrder.NONE)
    private var nameSortOrder by observable(SortOrder.NONE)
    private var activeThemeIndex by observable(0)
    private var currentPage by observable(1)
    private val pageSize = 3
    private var expandedRows by observable(setOf<Int>())
    private var selectedItems by observable(setOf<String>())
    private var searchQuery by observable("")
    private var searchSortOrder by observable(SortOrder.DESC)
    private var searchSortKey by observable("score")

    private val themes = listOf(TableTheme.Default, TableTheme.AntBlue, TableTheme.Teal, TableTheme.Dark)
    private val themeNames = listOf("Default", "Ant Blue", "Teal", "Dark")

    private val rawItems = listOf(
        TableItem("Alice", 95, "A"),
        TableItem("Bob", 82, "B"),
        TableItem("Carol", 78, "C+"),
        TableItem("Dave", 91, "A-"),
        TableItem("Eve", 67, "D+"),
        TableItem("Frank", 88, "B+"),
        TableItem("Grace", 74, "C"),
        TableItem("Henry", 99, "A+"),
    )

    private val wideItems = listOf(
        WideItem("Alice", "Engineering", "95", "A", "Top performer"),
        WideItem("Bob", "Design", "82", "B", "Consistent"),
        WideItem("Carol", "Product", "78", "C+", "Improving"),
        WideItem("Dave", "Engineering", "91", "A-", "Strong"),
        WideItem("Eve", "Marketing", "67", "D+", "Needs support"),
        WideItem("Frank", "Design", "88", "B+", "Creative"),
    )

    private fun sortedItems(): List<TableItem> = when {
        scoreSortOrder == SortOrder.ASC -> rawItems.sortedBy { it.score }
        scoreSortOrder == SortOrder.DESC -> rawItems.sortedByDescending { it.score }
        nameSortOrder == SortOrder.ASC -> rawItems.sortedBy { it.name }
        nameSortOrder == SortOrder.DESC -> rawItems.sortedByDescending { it.name }
        else -> rawItems
    }

    private fun pagedItems(): List<TableItem> {
        val sorted = sortedItems()
        val from = (currentPage - 1) * pageSize
        return sorted.drop(from).take(pageSize)
    }

    private fun totalPages(): Int = (rawItems.size + pageSize - 1) / pageSize

    override fun body(): ViewBuilder {
        val ctx = this
        return {
            attr {
                backgroundColor(Color.WHITE)
                flexDirectionColumn()
            }

            // Title bar
            View {
                attr {
                    height(56f)
                    backgroundColor(Color(0xFFF7F7F7L))
                    justifyContentCenter()
                    paddingLeft(16f)
                }
                Text {
                    attr {
                        fontSize(18f)
                        color(Color(0xFF1A1A1AL))
                        fontWeight700()
                        text("TableView Demo")
                    }
                }
            }

            // Theme switcher
            View {
                attr {
                    height(48f)
                    flexDirectionRow()
                    alignItemsCenter()
                    paddingLeft(16f)
                    paddingRight(16f)
                    backgroundColor(Color(0xFFFAFAFAL))
                    marginBottom(4f)
                }
                Text {
                    attr {
                        fontSize(13f)
                        color(Color(0xFF555555L))
                        text("主题: ")
                        marginRight(8f)
                    }
                }
                ctx.themeNames.forEachIndexed { index, name ->
                    val selected = index == ctx.activeThemeIndex
                    View {
                        attr {
                            height(28f)
                            paddingLeft(10f)
                            paddingRight(10f)
                            borderRadius(14f)
                            justifyContentCenter()
                            alignItemsCenter()
                            marginRight(8f)
                            backgroundColor(
                                if (selected) ctx.themes[index].headerBackground
                                else Color(0xFFE0E0E0L)
                            )
                        }
                        event { click { ctx.activeThemeIndex = index } }
                        Text {
                            attr {
                                fontSize(12f)
                                color(
                                    if (selected) ctx.themes[index].headerTextColor
                                    else Color(0xFF555555L)
                                )
                                text(name)
                            }
                        }
                    }
                }
            }

            // Section label
            View {
                attr {
                    height(36f)
                    backgroundColor(Color(0xFFEEEEEEL))
                    justifyContentCenter()
                    paddingLeft(16f)
                }
                Text {
                    attr {
                        fontSize(13f)
                        color(Color(0xFF555555L))
                        text("垂直滚动 - 主题表格（点列头排序）")
                    }
                }
            }

            // Themed sortable table
            Table {
                attr {
                    height(320f)
                    theme(ctx.themes[ctx.activeThemeIndex])
                    allowsSelection(true)
                }
                event {
                    rowClick { index ->
                        KLog.i("TableViewDemo", "row clicked: $index")
                    }
                }

                // Header row with sort controls
                ThemedHeaderRow(theme = ctx.themes[ctx.activeThemeIndex]) {
                    SortableHeaderCell(
                        text = "Name",
                        sortOrder = ctx.nameSortOrder,
                        theme = ctx.themes[ctx.activeThemeIndex],
                        onSortClick = {
                            ctx.currentPage = 1
                            ctx.scoreSortOrder = SortOrder.NONE
                            ctx.nameSortOrder = when (ctx.nameSortOrder) {
                                SortOrder.NONE, SortOrder.DESC -> SortOrder.ASC
                                SortOrder.ASC -> SortOrder.DESC
                            }
                        }
                    ) { attr { flex(2f) } }
                    SortableHeaderCell(
                        text = "Score",
                        sortOrder = ctx.scoreSortOrder,
                        theme = ctx.themes[ctx.activeThemeIndex],
                        onSortClick = {
                            ctx.currentPage = 1
                            ctx.nameSortOrder = SortOrder.NONE
                            ctx.scoreSortOrder = when (ctx.scoreSortOrder) {
                                SortOrder.NONE, SortOrder.DESC -> SortOrder.ASC
                                SortOrder.ASC -> SortOrder.DESC
                            }
                        }
                    ) { attr { flex(1f) } }
                    TableCell {
                        attr {
                            flex(1f)
                            justifyContentCenter()
                            alignItemsCenter()
                        }
                        Text {
                            attr {
                                fontSize(14f)
                                color(ctx.themes[ctx.activeThemeIndex].headerTextColor)
                                fontWeight700()
                                text("Grade")
                            }
                        }
                    }
                }

                // Empty state when no data
                if (ctx.pagedItems().isEmpty()) {
                    TableEmptyView(theme = ctx.themes[ctx.activeThemeIndex])
                }

                // Data rows (current page only)
                ctx.pagedItems().forEachIndexed { index, item ->
                    ThemedTableRow(theme = ctx.themes[ctx.activeThemeIndex], index = index) {
                        event {
                            click { KLog.i("TableViewDemo", "clicked: ${item.name}") }
                            selected { idx -> KLog.i("TableViewDemo", "selected $idx: ${item.name}") }
                            deselected { idx -> KLog.i("TableViewDemo", "deselected $idx") }
                        }
                        TableCell {
                            attr {
                                flex(2f)
                                justifyContentCenter()
                                paddingLeft(12f)
                            }
                            Text {
                                attr {
                                    fontSize(14f)
                                    color(Color(0xFF212121L))
                                    text(item.name)
                                }
                            }
                        }
                        TableCell {
                            attr {
                                flex(1f)
                                justifyContentCenter()
                                alignItemsCenter()
                            }
                            Text {
                                attr {
                                    fontSize(14f)
                                    color(Color(0xFF212121L))
                                    text(item.score.toString())
                                }
                            }
                        }
                        TableCell {
                            attr {
                                flex(1f)
                                justifyContentCenter()
                                alignItemsCenter()
                            }
                            View {
                                attr {
                                    paddingLeft(8f)
                                    paddingRight(8f)
                                    paddingTop(2f)
                                    paddingBottom(2f)
                                    borderRadius(4f)
                                    backgroundColor(gradeColor(item.grade))
                                }
                                Text {
                                    attr {
                                        fontSize(12f)
                                        color(Color(0xFFFFFFFFL))
                                        text(item.grade)
                                    }
                                }
                            }
                        }
                    }
                }

                // Summary row: avg score for current page
                if (ctx.pagedItems().isNotEmpty()) {
                    val avgScore = ctx.pagedItems().map { it.score }.average().toInt()
                    ThemedSummaryRow(theme = ctx.themes[ctx.activeThemeIndex]) {
                        TableCell {
                            attr { flex(2f); justifyContentCenter(); paddingLeft(12f) }
                            Text {
                                attr { fontSize(13f); color(Color(0xFF555555L)); text("平均") }
                            }
                        }
                        TableCell {
                            attr { flex(1f); justifyContentCenter(); alignItemsCenter() }
                            Text {
                                attr { fontSize(13f); color(Color(0xFF555555L)); text("$avgScore") }
                            }
                        }
                        TableCell {
                            attr { flex(1f); justifyContentCenter(); alignItemsCenter() }
                            Text {
                                attr { fontSize(12f); color(Color(0xFF999999L)); text("avg") }
                            }
                        }
                    }
                }
            }

            // Pagination bar below the sortable table
            PaginationBar(
                currentPage = ctx.currentPage,
                totalPages = ctx.totalPages(),
                theme = ctx.themes[ctx.activeThemeIndex],
                showTotal = true,
                totalItems = ctx.rawItems.size,
                onPageChange = { ctx.currentPage = it },
            )

            // HTable section
            ctx.addHTableSection(this)

            // Expandable + grouped-header section
            ctx.addExpandableSection(this)

            // Multi-select checkbox section
            ctx.addCheckboxSection(this)

            // Search + sort section
            ctx.addSearchSortSection(this)

            // Swipeable rows section
            ctx.addSwipeableSection(this)
        }
    }

    private fun addHTableSection(container: ViewContainer<*, *>) {
        val ctx = this
        container.apply {
            View {
                attr {
                    height(40f)
                    backgroundColor(Color(0xFFF7F7F7L))
                    justifyContentCenter()
                    paddingLeft(16f)
                    marginTop(12f)
                }
                Text {
                    attr {
                        fontSize(14f)
                        color(Color(0xFF1A1A1AL))
                        fontWeight700()
                        text("双向滚动 - HTable（向右划）")
                    }
                }
            }
            HTable(tableWidth = 700f) {
                attr {
                    flex(1f)
                    separatorColor(Color(0xFFE0E0E0L))
                    separatorHeight(0.5f)
                }
                TableRow {
                    attr {
                        rowHeight(44f)
                        backgroundColor(Color(0xFFF5F5F5L))
                        flexDirectionRow()
                    }
                    for (header in listOf("Name" to 120f, "Dept" to 160f, "Score" to 80f, "Grade" to 80f, "Note" to 260f)) {
                        TableCell {
                            attr {
                                width(header.second)
                                justifyContentCenter()
                                paddingLeft(12f)
                            }
                            Text {
                                attr {
                                    fontSize(13f)
                                    color(Color(0xFF333333L))
                                    fontWeight700()
                                    text(header.first)
                                }
                            }
                        }
                    }
                }
                ctx.wideItems.forEachIndexed { idx, item ->
                    TableRow {
                        attr {
                            rowHeight(48f)
                            flexDirectionRow()
                            backgroundColor(if (idx % 2 == 0) Color(0xFFFFFFFFL) else Color(0xFFFAFAFAL))
                        }
                        event {
                            rowClick { _ -> KLog.i("HTableDemo", "clicked: ${item.name}") }
                        }
                        for (cell in listOf(item.name to 120f, item.dept to 160f, item.score to 80f, item.grade to 80f, item.note to 260f)) {
                            TableCell {
                                attr {
                                    width(cell.second)
                                    justifyContentCenter()
                                    paddingLeft(12f)
                                }
                                Text {
                                    attr {
                                        fontSize(13f)
                                        color(Color(0xFF212121L))
                                        text(cell.first)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun addExpandableSection(container: ViewContainer<*, *>) {
        val ctx = this
        container.apply {
            View {
                attr {
                    height(40f)
                    backgroundColor(Color(0xFFF7F7F7L))
                    justifyContentCenter()
                    paddingLeft(16f)
                    marginTop(12f)
                }
                Text {
                    attr {
                        fontSize(14f)
                        color(Color(0xFF1A1A1AL))
                        fontWeight700()
                        text("分组列头 + 可展开行")
                    }
                }
            }
            Table {
                attr {
                    height(320f)
                    theme(ctx.themes[ctx.activeThemeIndex])
                }
                TableHeaderGroup(ctx.themes[ctx.activeThemeIndex]) {
                    TableGroupCell("基本信息", ctx.themes[ctx.activeThemeIndex]) {
                        attr { flex(3f) }
                    }
                    TableGroupCell("详情", ctx.themes[ctx.activeThemeIndex]) {
                        attr { flex(2f) }
                    }
                }
                ctx.rawItems.take(4).forEachIndexed { i, item ->
                    ExpandableTableRow(
                        theme = ctx.themes[ctx.activeThemeIndex],
                        index = i,
                        expanded = i in ctx.expandedRows,
                        onToggle = {
                            ctx.expandedRows = if (i in ctx.expandedRows)
                                ctx.expandedRows - i
                            else
                                ctx.expandedRows + i
                        },
                        expandedContent = {
                            Text {
                                attr {
                                    fontSize(13f)
                                    color(Color(0xFF555555L))
                                    text("${item.name} 的详情：分数 ${item.score}，等级 ${item.grade}，表现优秀。")
                                }
                            }
                        }
                    ) {
                        TableCell {
                            attr { flex(2f); justifyContentCenter(); paddingLeft(8f) }
                            Text {
                                attr { fontSize(14f); color(Color(0xFF212121L)); text(item.name) }
                            }
                        }
                        TableCell {
                            attr { flex(1f); justifyContentCenter(); alignItemsCenter() }
                            Text {
                                attr { fontSize(14f); color(Color(0xFF212121L)); text(item.score.toString()) }
                            }
                        }
                        TableCell {
                            attr { flex(1f); justifyContentCenter(); alignItemsCenter() }
                            View {
                                attr {
                                    paddingLeft(6f); paddingRight(6f)
                                    paddingTop(2f); paddingBottom(2f)
                                    borderRadius(4f)
                                    backgroundColor(ctx.gradeColor(item.grade))
                                }
                                Text {
                                    attr { fontSize(12f); color(Color(0xFFFFFFFFL)); text(item.grade) }
                                }
                            }
                        }
                        TableCell { attr { flex(1f) } }
                    }
                }
            }
        }
    }

    private fun addCheckboxSection(container: ViewContainer<*, *>) {
        val ctx = this
        val theme = ctx.themes[ctx.activeThemeIndex]
        container.apply {
            View {
                attr {
                    height(40f)
                    backgroundColor(Color(0xFFF7F7F7L))
                    justifyContentCenter()
                    paddingLeft(16f)
                    marginTop(12f)
                }
                Text {
                    attr {
                        fontSize(14f)
                        color(Color(0xFF1A1A1AL))
                        fontWeight700()
                        text("多选 - CheckboxTableRow + 批量操作栏")
                    }
                }
            }

            // Batch action bar (visible when any row selected)
            TableBatchActionBar(
                selectedCount = ctx.selectedItems.size,
                totalCount = ctx.rawItems.size,
                theme = theme,
                onSelectAll = {
                    ctx.selectedItems = ctx.rawItems.map { it.name }.toSet()
                },
                onClearAll = {
                    ctx.selectedItems = emptySet()
                },
                actions = listOf(
                    "删除" to {
                        KLog.i("CheckboxDemo", "delete: ${ctx.selectedItems}")
                        ctx.selectedItems = emptySet()
                    }
                ),
            )

            // Header row
            Table {
                attr {
                    height(320f)
                    theme(theme)
                }
                ThemedHeaderRow(theme = theme) {
                    // Checkbox placeholder header cell
                    TableCell {
                        attr {
                            width(40f)
                            justifyContentCenter()
                            alignItemsCenter()
                        }
                        // select-all checkbox in header
                        View {
                            attr {
                                width(18f)
                                height(18f)
                                borderRadius(3f)
                                justifyContentCenter()
                                alignItemsCenter()
                                backgroundColor(
                                    if (ctx.selectedItems.size == ctx.rawItems.size && ctx.rawItems.isNotEmpty())
                                        Color(0xFFFFFFFFL)
                                    else
                                        Color(red255 = 255, green255 = 255, blue255 = 255, alpha01 = 0.3f)
                                )
                            }
                            event {
                                click {
                                    ctx.selectedItems = if (ctx.selectedItems.size == ctx.rawItems.size)
                                        emptySet()
                                    else
                                        ctx.rawItems.map { it.name }.toSet()
                                }
                            }
                            if (ctx.selectedItems.size == ctx.rawItems.size && ctx.rawItems.isNotEmpty()) {
                                Text {
                                    attr {
                                        fontSize(11f)
                                        color(theme.headerBackground)
                                        text("✓")
                                    }
                                }
                            }
                        }
                    }
                    TableCell {
                        attr { flex(2f); justifyContentCenter(); paddingLeft(4f) }
                        Text {
                            attr {
                                fontSize(14f)
                                color(theme.headerTextColor)
                                fontWeight700()
                                text("Name")
                            }
                        }
                    }
                    TableCell {
                        attr { flex(1f); justifyContentCenter(); alignItemsCenter() }
                        Text {
                            attr {
                                fontSize(14f)
                                color(theme.headerTextColor)
                                fontWeight700()
                                text("Score")
                            }
                        }
                    }
                    TableCell {
                        attr { flex(1f); justifyContentCenter(); alignItemsCenter() }
                        Text {
                            attr {
                                fontSize(14f)
                                color(theme.headerTextColor)
                                fontWeight700()
                                text("Grade")
                            }
                        }
                    }
                }

                ctx.rawItems.forEachIndexed { index, item ->
                    val selected = item.name in ctx.selectedItems
                    CheckboxTableRow(
                        theme = theme,
                        index = index,
                        selected = selected,
                        onToggle = {
                            ctx.selectedItems = if (selected)
                                ctx.selectedItems - item.name
                            else
                                ctx.selectedItems + item.name
                        }
                    ) {
                        TableCell {
                            attr { flex(2f); justifyContentCenter(); paddingLeft(4f) }
                            Text {
                                attr {
                                    fontSize(14f)
                                    color(Color(0xFF212121L))
                                    text(item.name)
                                }
                            }
                        }
                        TableCell {
                            attr { flex(1f); justifyContentCenter(); alignItemsCenter() }
                            Text {
                                attr {
                                    fontSize(14f)
                                    color(Color(0xFF212121L))
                                    text(item.score.toString())
                                }
                            }
                        }
                        TableCell {
                            attr { flex(1f); justifyContentCenter(); alignItemsCenter() }
                            View {
                                attr {
                                    paddingLeft(6f)
                                    paddingRight(6f)
                                    paddingTop(2f)
                                    paddingBottom(2f)
                                    borderRadius(4f)
                                    backgroundColor(ctx.gradeColor(item.grade))
                                }
                                Text {
                                    attr {
                                        fontSize(12f)
                                        color(Color(0xFFFFFFFFL))
                                        text(item.grade)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Section: sticky header table
            View {
                attr {
                    height(36f)
                    backgroundColor(Color(0xFFEEEEEEL))
                    justifyContentCenter()
                    paddingLeft(16f)
                    marginTop(8f)
                }
                Text {
                    attr {
                        fontSize(13f)
                        color(Color(0xFF555555L))
                        text("粘性表头 StickyTable")
                    }
                }
            }
            StickyTable(
                height = 240f,
                theme = ctx.themes[ctx.activeThemeIndex],
                headerContent = {
                    TableCell {
                        attr { flex(2f); justifyContentCenter(); paddingLeft(12f) }
                        Text {
                            attr {
                                fontSize(14f)
                                color(ctx.themes[ctx.activeThemeIndex].headerTextColor)
                                fontWeight700()
                                text("Name")
                            }
                        }
                    }
                    TableCell {
                        attr { flex(1f); justifyContentCenter(); alignItemsCenter() }
                        Text {
                            attr {
                                fontSize(14f)
                                color(ctx.themes[ctx.activeThemeIndex].headerTextColor)
                                fontWeight700()
                                text("Score")
                            }
                        }
                    }
                    TableCell {
                        attr { flex(1f); justifyContentCenter(); alignItemsCenter() }
                        Text {
                            attr {
                                fontSize(14f)
                                color(ctx.themes[ctx.activeThemeIndex].headerTextColor)
                                fontWeight700()
                                text("Grade")
                            }
                        }
                    }
                },
                bodyContent = {
                    ctx.rawItems.forEachIndexed { idx, item ->
                        ThemedTableRow(theme = ctx.themes[ctx.activeThemeIndex], index = idx) {
                            TableCell {
                                attr { flex(2f); justifyContentCenter(); paddingLeft(12f) }
                                Text { attr { fontSize(14f); color(Color(0xFF212121L)); text(item.name) } }
                            }
                            TableCell {
                                attr { flex(1f); justifyContentCenter(); alignItemsCenter() }
                                Text { attr { fontSize(14f); color(Color(0xFF212121L)); text(item.score.toString()) } }
                            }
                            TableCell {
                                attr { flex(1f); justifyContentCenter(); alignItemsCenter() }
                                View {
                                    attr {
                                        paddingLeft(6f); paddingRight(6f)
                                        paddingTop(2f); paddingBottom(2f)
                                        borderRadius(4f)
                                        backgroundColor(ctx.gradeColor(item.grade))
                                    }
                                    Text { attr { fontSize(12f); color(Color(0xFFFFFFFFL)); text(item.grade) } }
                                }
                            }
                        }
                    }
                }
            )

            // Section: frozen first column table
            View {
                attr {
                    height(36f)
                    backgroundColor(Color(0xFFEEEEEEL))
                    justifyContentCenter()
                    paddingLeft(16f)
                    marginTop(8f)
                }
                Text {
                    attr {
                        fontSize(13f)
                        color(Color(0xFF555555L))
                        text("首列冻结 FrozenColumnTable")
                    }
                }
            }
            FrozenColumnTable(
                rowCount = ctx.wideItems.size,
                rowHeight = 48f,
                frozenWidth = 70f,
                theme = ctx.themes[ctx.activeThemeIndex],
                frozenHeaderContent = {
                    Text {
                        attr {
                            fontSize(14f)
                            color(ctx.themes[ctx.activeThemeIndex].headerTextColor)
                            fontWeight700()
                            text("Name")
                        }
                    }
                },
                frozenRowContent = { idx ->
                    {
                        Text {
                            attr {
                                fontSize(13f)
                                color(Color(0xFF212121L))
                                text(ctx.wideItems[idx].name)
                            }
                        }
                    }
                },
                scrollHeaderContent = {
                    TableCell {
                        attr { width(100f); justifyContentCenter(); paddingLeft(8f) }
                        Text {
                            attr {
                                fontSize(14f)
                                color(ctx.themes[ctx.activeThemeIndex].headerTextColor)
                                fontWeight700()
                                text("Dept")
                            }
                        }
                    }
                    TableCell {
                        attr { width(70f); justifyContentCenter(); alignItemsCenter() }
                        Text {
                            attr {
                                fontSize(14f)
                                color(ctx.themes[ctx.activeThemeIndex].headerTextColor)
                                fontWeight700()
                                text("Score")
                            }
                        }
                    }
                    TableCell {
                        attr { width(60f); justifyContentCenter(); alignItemsCenter() }
                        Text {
                            attr {
                                fontSize(14f)
                                color(ctx.themes[ctx.activeThemeIndex].headerTextColor)
                                fontWeight700()
                                text("Grade")
                            }
                        }
                    }
                    TableCell {
                        attr { width(130f); justifyContentCenter(); paddingLeft(8f) }
                        Text {
                            attr {
                                fontSize(14f)
                                color(ctx.themes[ctx.activeThemeIndex].headerTextColor)
                                fontWeight700()
                                text("Note")
                            }
                        }
                    }
                },
                scrollRowContent = { idx ->
                    {
                        TableCell {
                            attr { width(100f); justifyContentCenter(); paddingLeft(8f) }
                            Text { attr { fontSize(13f); color(Color(0xFF212121L)); text(ctx.wideItems[idx].dept) } }
                        }
                        TableCell {
                            attr { width(70f); justifyContentCenter(); alignItemsCenter() }
                            Text { attr { fontSize(13f); color(Color(0xFF212121L)); text(ctx.wideItems[idx].score) } }
                        }
                        TableCell {
                            attr { width(60f); justifyContentCenter(); alignItemsCenter() }
                            View {
                                attr {
                                    paddingLeft(6f); paddingRight(6f)
                                    paddingTop(2f); paddingBottom(2f)
                                    borderRadius(4f)
                                    backgroundColor(ctx.gradeColor(ctx.wideItems[idx].grade))
                                }
                                Text { attr { fontSize(11f); color(Color(0xFFFFFFFFL)); text(ctx.wideItems[idx].grade) } }
                            }
                        }
                        TableCell {
                            attr { width(130f); justifyContentCenter(); paddingLeft(8f) }
                            Text { attr { fontSize(13f); color(Color(0xFF757575L)); text(ctx.wideItems[idx].note) } }
                        }
                    }
                }
            )

            // Bottom padding
            View { attr { height(32f) } }
        }
    }

    private fun addSearchSortSection(container: ViewContainer<*, *>) {
        val ctx = this
        container.apply {
            View {
                attr {
                    height(40f)
                    backgroundColor(Color(0xFFF7F7F7L))
                    justifyContentCenter()
                    paddingLeft(16f)
                    marginTop(12f)
                }
                Text {
                    attr {
                        fontSize(14f)
                        color(Color(0xFF1A1A1AL))
                        fontWeight700()
                        text("搜索 + 排序 - TableSearchBar")
                    }
                }
            }

            // Live search bar
            TableSearchBar(
                query = ctx.searchQuery,
                theme = ctx.themes[ctx.activeThemeIndex],
                placeholder = "搜索姓名…",
                onQueryChange = { ctx.searchQuery = it },
                onClear = { ctx.searchQuery = "" },
            )

            // Filtered + sorted data
            val filtered = ctx.rawItems.filter {
                ctx.searchQuery.isEmpty() || it.name.contains(ctx.searchQuery, ignoreCase = true)
            }
            val sorted = when {
                ctx.searchSortKey == "name" && ctx.searchSortOrder == SortOrder.ASC -> filtered.sortedBy { it.name }
                ctx.searchSortKey == "name" && ctx.searchSortOrder == SortOrder.DESC -> filtered.sortedByDescending { it.name }
                ctx.searchSortKey == "score" && ctx.searchSortOrder == SortOrder.ASC -> filtered.sortedBy { it.score }
                ctx.searchSortKey == "score" && ctx.searchSortOrder == SortOrder.DESC -> filtered.sortedByDescending { it.score }
                ctx.searchSortKey == "grade" && ctx.searchSortOrder == SortOrder.ASC -> filtered.sortedBy { it.grade }
                ctx.searchSortKey == "grade" && ctx.searchSortOrder == SortOrder.DESC -> filtered.sortedByDescending { it.grade }
                else -> filtered
            }

            val theme = ctx.themes[ctx.activeThemeIndex]
            Table {
                attr {
                    height(280f)
                    theme(theme)
                }
                ThemedHeaderRow(theme = theme) {
                    SortableHeaderCell(
                        text = "Name",
                        sortOrder = if (ctx.searchSortKey == "name") ctx.searchSortOrder else SortOrder.NONE,
                        theme = theme,
                        onSortClick = {
                            val next = if (ctx.searchSortKey == "name") when (ctx.searchSortOrder) {
                                SortOrder.NONE, SortOrder.DESC -> SortOrder.ASC
                                SortOrder.ASC -> SortOrder.DESC
                            } else SortOrder.ASC
                            ctx.searchSortKey = "name"
                            ctx.searchSortOrder = next
                        }
                    ) { attr { flex(2f) } }
                    SortableHeaderCell(
                        text = "Score",
                        sortOrder = if (ctx.searchSortKey == "score") ctx.searchSortOrder else SortOrder.NONE,
                        theme = theme,
                        onSortClick = {
                            val next = if (ctx.searchSortKey == "score") when (ctx.searchSortOrder) {
                                SortOrder.NONE, SortOrder.DESC -> SortOrder.ASC
                                SortOrder.ASC -> SortOrder.DESC
                            } else SortOrder.ASC
                            ctx.searchSortKey = "score"
                            ctx.searchSortOrder = next
                        }
                    ) { attr { flex(1f) } }
                    SortableHeaderCell(
                        text = "Grade",
                        sortOrder = if (ctx.searchSortKey == "grade") ctx.searchSortOrder else SortOrder.NONE,
                        theme = theme,
                        onSortClick = {
                            val next = if (ctx.searchSortKey == "grade") when (ctx.searchSortOrder) {
                                SortOrder.NONE, SortOrder.DESC -> SortOrder.ASC
                                SortOrder.ASC -> SortOrder.DESC
                            } else SortOrder.ASC
                            ctx.searchSortKey = "grade"
                            ctx.searchSortOrder = next
                        }
                    ) { attr { flex(1f) } }
                }
                if (sorted.isEmpty()) {
                    TableEmptyView(theme = theme, message = if (ctx.searchQuery.isEmpty()) "暂无数据" else "无匹配结果 \"${ctx.searchQuery}\"")
                }
                sorted.forEachIndexed { index, item ->
                    ThemedTableRow(theme = theme, index = index) {
                        TableCell {
                            attr { flex(2f); justifyContentCenter(); paddingLeft(12f) }
                            Text {
                                attr { fontSize(14f); color(Color(0xFF212121L)); text(item.name) }
                            }
                        }
                        TableCell {
                            attr { flex(1f); justifyContentCenter(); alignItemsCenter() }
                            Text {
                                attr { fontSize(14f); color(Color(0xFF212121L)); text(item.score.toString()) }
                            }
                        }
                        TableCell {
                            attr { flex(1f); justifyContentCenter(); alignItemsCenter() }
                            View {
                                attr {
                                    paddingLeft(6f); paddingRight(6f)
                                    paddingTop(2f); paddingBottom(2f)
                                    borderRadius(4f)
                                    backgroundColor(ctx.gradeColor(item.grade))
                                }
                                Text {
                                    attr { fontSize(12f); color(Color(0xFFFFFFFFL)); text(item.grade) }
                                }
                            }
                        }
                    }
                }
            }

            View { attr { height(40f) } }
        }
    }

    private fun addSwipeableSection(container: ViewContainer<*, *>) {
        val ctx = this
        container.apply {
            View {
                attr {
                    height(40f)
                    backgroundColor(Color(0xFFF7F7F7L))
                    justifyContentCenter()
                    paddingLeft(16f)
                    marginTop(12f)
                }
                Text {
                    attr {
                        fontSize(14f)
                        color(Color(0xFF1A1A1AL))
                        fontWeight700()
                        text("左划显示操作 - SwipeableTableRow")
                    }
                }
            }

            Table {
                attr {
                    height(280f)
                    theme(ctx.themes[ctx.activeThemeIndex])
                }

                // Row 1: right actions only - Delete + Archive
                SwipeableTableRow {
                    attr {
                        rowHeight(56f)
                        theme(ctx.themes[ctx.activeThemeIndex])
                        rightActions(
                            SwipeAction("删除", Color(0xFFF44336L), icon = "🗑"),
                            SwipeAction("归档", Color(0xFF2196F3L), icon = "📦"),
                        )
                        content {
                            View {
                                attr {
                                    flex(1f)
                                    flexDirectionRow()
                                    alignItemsCenter()
                                    paddingLeft(16f)
                                    paddingRight(16f)
                                }
                                Text {
                                    attr {
                                        fontSize(15f)
                                        color(Color(0xFF212121L))
                                        flex(1f)
                                        text("← 向左滑动查看操作")
                                    }
                                }
                                Text {
                                    attr {
                                        fontSize(12f)
                                        color(Color(0xFFAAAAAAL))
                                        text("删除 / 归档")
                                    }
                                }
                            }
                        }
                    }
                    event {
                        onAction { side, index ->
                            KLog.i("SwipeDemo", "action: $side[$index]")
                        }
                    }
                }

                // Row 2: left + right actions - Star (left), Delete + More (right)
                SwipeableTableRow {
                    attr {
                        rowHeight(56f)
                        theme(ctx.themes[ctx.activeThemeIndex])
                        leftActions(
                            SwipeAction("收藏", Color(0xFFFFC107L), icon = "⭐"),
                        )
                        rightActions(
                            SwipeAction("删除", Color(0xFFF44336L), icon = "🗑"),
                            SwipeAction("更多", Color(0xFF9E9E9EL), icon = "···"),
                        )
                        content {
                            View {
                                attr {
                                    flex(1f)
                                    flexDirectionRow()
                                    alignItemsCenter()
                                    paddingLeft(16f)
                                    paddingRight(16f)
                                }
                                Text {
                                    attr {
                                        fontSize(15f)
                                        color(Color(0xFF212121L))
                                        flex(1f)
                                        text("← 左右均可滑动 →")
                                    }
                                }
                                Text {
                                    attr {
                                        fontSize(12f)
                                        color(Color(0xFFAAAAAAL))
                                        text("收藏 / 删除 / 更多")
                                    }
                                }
                            }
                        }
                    }
                    event {
                        onAction { side, index ->
                            KLog.i("SwipeDemo", "action: $side[$index]")
                        }
                    }
                }

                // Row 3: right actions with wide buttons
                SwipeableTableRow {
                    attr {
                        rowHeight(56f)
                        theme(ctx.themes[ctx.activeThemeIndex])
                        rightActions(
                            SwipeAction("标记已读", Color(0xFF4CAF50L), width = 90f),
                        )
                        content {
                            View {
                                attr {
                                    flex(1f)
                                    flexDirectionRow()
                                    alignItemsCenter()
                                    paddingLeft(16f)
                                    paddingRight(16f)
                                }
                                Text {
                                    attr {
                                        fontSize(15f)
                                        color(Color(0xFF212121L))
                                        flex(1f)
                                        text("← 左划标记已读")
                                    }
                                }
                            }
                        }
                    }
                    event {
                        onAction { side, index ->
                            KLog.i("SwipeDemo", "action: $side[$index]")
                        }
                    }
                }

                // Row 4: right actions with wide buttons
                SwipeableTableRow {
                    attr {
                        rowHeight(56f)
                        theme(ctx.themes[ctx.activeThemeIndex])
                        rightActions(
                            SwipeAction("置顶", Color(0xFF7B1FA2L), icon = "↑", width = 64f),
                            SwipeAction("删除", Color(0xFFF44336L), icon = "✕", width = 64f),
                        )
                        content {
                            View {
                                attr {
                                    flex(1f)
                                    flexDirectionRow()
                                    alignItemsCenter()
                                    paddingLeft(16f)
                                    paddingRight(16f)
                                }
                                Text {
                                    attr {
                                        fontSize(15f)
                                        color(Color(0xFF212121L))
                                        flex(1f)
                                        text("← 左划置顶或删除")
                                    }
                                }
                            }
                        }
                    }
                    event {
                        onAction { side, index ->
                            KLog.i("SwipeDemo", "action: $side[$index]")
                        }
                    }
                }
            }

            View { attr { height(40f) } }
        }
    }

    private fun gradeColor(grade: String): Color = when {
        grade.startsWith("A") -> Color(0xFF4CAF50L)
        grade.startsWith("B") -> Color(0xFF2196F3L)
        grade.startsWith("C") -> Color(0xFFFF9800L)
        else -> Color(0xFFF44336L)
    }
}
