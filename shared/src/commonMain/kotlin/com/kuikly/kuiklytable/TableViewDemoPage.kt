package com.kuikly.kuiklytable

import com.tencent.kuikly.core.annotations.Page
import com.tencent.kuikly.core.base.Color
import com.tencent.kuikly.core.base.ViewBuilder
import com.tencent.kuikly.core.log.KLog
import com.tencent.kuikly.core.views.Text
import com.tencent.kuikly.core.views.View
import com.tencent.kuiklybase.table.HTable
import com.tencent.kuiklybase.table.Table
import com.tencent.kuiklybase.table.TableCell
import com.tencent.kuiklybase.table.TableRow
import com.kuikly.kuiklytable.base.BasePager

private data class TableItem(val name: String, val score: String, val grade: String)
private data class WideItem(val name: String, val dept: String, val score: String, val grade: String, val note: String)

@Page("TableViewDemoPage")
internal class TableViewDemoPage : BasePager() {

    private val items = listOf(
        TableItem("Alice", "95", "A"),
        TableItem("Bob", "82", "B"),
        TableItem("Carol", "78", "C+"),
        TableItem("Dave", "91", "A-"),
        TableItem("Eve", "67", "D+"),
        TableItem("Frank", "88", "B+"),
        TableItem("Grace", "74", "C"),
        TableItem("Henry", "99", "A+"),
    )

    private val wideItems = listOf(
        WideItem("Alice", "Engineering", "95", "A", "Top performer"),
        WideItem("Bob", "Design", "82", "B", "Consistent"),
        WideItem("Carol", "Product", "78", "C+", "Improving"),
        WideItem("Dave", "Engineering", "91", "A-", "Strong"),
        WideItem("Eve", "Marketing", "67", "D+", "Needs support"),
        WideItem("Frank", "Design", "88", "B+", "Creative"),
    )

    override fun body(): ViewBuilder {
        val ctx = this
        return {
            attr {
                backgroundColor(Color.WHITE)
                flexDirectionColumn()
            }
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
                        text("垂直滚动 - Table")
                    }
                }
            }
            Table {
                attr {
                    height(280f)
                    separatorColor(Color(0xFFE0E0E0L))
                    separatorHeight(0.5f)
                    allowsSelection(true)
                }
                event {
                    rowClick { index ->
                        KLog.i("TableViewDemo", "row clicked: $index")
                    }
                }
                TableRow {
                    attr {
                        rowHeight(44f)
                        backgroundColor(Color(0xFFF5F5F5L))
                        flexDirectionRow()
                    }
                    TableCell {
                        attr {
                            flex(2f)
                            justifyContentCenter()
                            paddingLeft(16f)
                        }
                        Text {
                            attr {
                                fontSize(14f)
                                color(Color(0xFF333333L))
                                fontWeight700()
                                text("Name")
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
                                color(Color(0xFF333333L))
                                fontWeight700()
                                text("Score")
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
                                color(Color(0xFF333333L))
                                fontWeight700()
                                text("Grade")
                            }
                        }
                    }
                }
                ctx.items.forEachIndexed { index, item ->
                    TableRow {
                        attr {
                            rowHeight(48f)
                            flexDirectionRow()
                            backgroundColor(
                                if (index % 2 == 0) Color.WHITE else Color(0xFFFAFAFAL)
                            )
                        }
                        event {
                            click {
                                KLog.i("TableViewDemo", "clicked item: ${item.name}")
                            }
                            selected { idx ->
                                KLog.i("TableViewDemo", "row $idx selected: ${item.name}")
                            }
                            deselected { idx ->
                                KLog.i("TableViewDemo", "row $idx deselected")
                            }
                        }
                        TableCell {
                            attr {
                                flex(2f)
                                justifyContentCenter()
                                paddingLeft(16f)
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
                                    text(item.score)
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
                                        color(Color.WHITE)
                                        text(item.grade)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            ctx.addHTableSection(this)
        }
    }

    private fun addHTableSection(container: com.tencent.kuikly.core.base.ViewContainer<*, *>) {
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
                // Header
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
                            backgroundColor(if (idx % 2 == 0) Color.WHITE else Color(0xFFFAFAFAL))
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

    private fun gradeColor(grade: String): Color = when {
        grade.startsWith("A") -> Color(0xFF4CAF50L)
        grade.startsWith("B") -> Color(0xFF2196F3L)
        grade.startsWith("C") -> Color(0xFFFF9800L)
        else -> Color(0xFFF44336L)
    }
}
