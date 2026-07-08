package com.kuikly.kuiklytable

import com.tencent.kuikly.core.annotations.Page
import com.tencent.kuikly.core.base.Color
import com.tencent.kuikly.core.base.ViewBuilder
import com.tencent.kuikly.core.log.KLog
import com.tencent.kuikly.core.views.Text
import com.tencent.kuikly.core.views.View
import com.tencent.kuiklybase.table.Table
import com.tencent.kuiklybase.table.TableCell
import com.tencent.kuiklybase.table.TableRow
import com.kuikly.kuiklytable.base.BasePager

private data class TableItem(val name: String, val score: String, val grade: String)

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
            Table {
                attr {
                    flex(1f)
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
        }
    }

    private fun gradeColor(grade: String): Color = when {
        grade.startsWith("A") -> Color(0xFF4CAF50L)
        grade.startsWith("B") -> Color(0xFF2196F3L)
        grade.startsWith("C") -> Color(0xFFFF9800L)
        else -> Color(0xFFF44336L)
    }
}
