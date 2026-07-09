/*
 * Tencent is pleased to support the open source community by making KuiklyUI
 * available.
 * Copyright (C) 2025 Tencent. All rights reserved.
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
import com.tencent.kuikly.core.base.ViewConst
import com.tencent.kuikly.core.base.ViewContainer
import com.tencent.kuikly.core.base.toInt
import com.tencent.kuikly.core.layout.FlexDirection
import com.tencent.kuikly.core.log.KLog
import com.tencent.kuikly.core.views.Scroller
import com.tencent.kuikly.core.views.View
import com.tencent.kuikly.core.views.internal.GroupAttr
import com.tencent.kuikly.core.views.internal.GroupEvent
import com.tencent.kuikly.core.views.internal.GroupView

fun ViewContainer<*, *>.Table(init: TableView.() -> Unit) {
    addChild(TableView(), init)
}

/**
 * Bidirectional-scrolling table: horizontal panning via outer [Scroller],
 * vertical scrolling via the native [TableView].
 *
 * [tableWidth] must exceed the viewport width for horizontal overflow to work.
 */
fun ViewContainer<*, *>.HTable(tableWidth: Float, init: TableView.() -> Unit) {
    Scroller {
        attr { flexDirection(FlexDirection.ROW) }
        View {
            attr { width(tableWidth) }
            addChild(TableView(), init)
        }
    }
}

fun ViewContainer<*, *>.TableRow(init: TableRowView.() -> Unit) {
    addChild(TableRowView(), init)
}

fun ViewContainer<*, *>.TableCell(init: TableCellView.() -> Unit) {
    addChild(TableCellView(), init)
}

open class TableView : GroupView<TableAttr, TableEvent>() {

    override fun createAttr(): TableAttr = TableAttr()

    override fun createEvent(): TableEvent = TableEvent()

    override fun viewName(): String = ViewConst.TYPE_TABLE

    override fun isRenderView(): Boolean = true

    fun TableRow(init: TableRowView.() -> Unit) {
        addChild(TableRowView(), init)
    }
}

open class TableAttr : GroupAttr() {

    fun separatorColor(color: Color) {
        "separatorColor" with color.hexColor
    }

    fun separatorHeight(height: Float) {
        "separatorHeight" with height
    }

    fun allowsMultipleSelection(allow: Boolean) {
        "allowsMultipleSelection" with allow.toInt()
    }

    fun allowsSelection(allow: Boolean) {
        "allowsSelection" with allow.toInt()
    }
}

open class TableEvent : GroupEvent() {

    fun rowClick(handler: (index: Int) -> Unit) {
        register("rowClick") { data ->
            val index = (data as? Number)?.toInt()
            if (index == null) {
                KLog.e("TableEvent", "rowClick: unexpected data type ${data?.let { it::class.simpleName }}")
                return@register
            }
            handler(index)
        }
    }

    fun rowLongPress(handler: (index: Int) -> Unit) {
        register("rowLongPress") { data ->
            val index = (data as? Number)?.toInt()
            if (index == null) {
                KLog.e("TableEvent", "rowLongPress: unexpected data type ${data?.let { it::class.simpleName }}")
                return@register
            }
            handler(index)
        }
    }

    fun reachEnd(handler: () -> Unit) {
        register("reachEnd") { _ -> handler() }
    }

    fun scroll(handler: (offsetY: Float) -> Unit) {
        register("scroll") { data ->
            val offset = (data as? Number)?.toFloat() ?: 0f
            handler(offset)
        }
    }
}

open class TableRowView : GroupView<TableRowAttr, TableRowEvent>() {

    override fun createAttr(): TableRowAttr = TableRowAttr()

    override fun createEvent(): TableRowEvent = TableRowEvent()

    override fun viewName(): String = ViewConst.TYPE_TABLE_ROW

    override fun isRenderView(): Boolean = true

    fun TableCell(init: TableCellView.() -> Unit) {
        addChild(TableCellView(), init)
    }
}

open class TableRowAttr : GroupAttr() {

    fun rowHeight(height: Float) {
        "rowHeight" with height
    }

    fun selectable(selectable: Boolean) {
        "selectable" with selectable.toInt()
    }
}

open class TableRowEvent : GroupEvent() {

    fun selected(handler: (index: Int) -> Unit) {
        register("selected") { data ->
            val index = (data as? Number)?.toInt()
            if (index == null) {
                KLog.e("TableRowEvent", "selected: unexpected data type ${data?.let { it::class.simpleName }}")
                return@register
            }
            handler(index)
        }
    }

    fun deselected(handler: (index: Int) -> Unit) {
        register("deselected") { data ->
            val index = (data as? Number)?.toInt()
            if (index == null) {
                KLog.e("TableRowEvent", "deselected: unexpected data type ${data?.let { it::class.simpleName }}")
                return@register
            }
            handler(index)
        }
    }
}

open class TableCellView : GroupView<TableCellAttr, TableCellEvent>() {

    override fun createAttr(): TableCellAttr = TableCellAttr()

    override fun createEvent(): TableCellEvent = TableCellEvent()

    override fun viewName(): String = ViewConst.TYPE_TABLE_CELL

    override fun isRenderView(): Boolean = true
}

open class TableCellAttr : GroupAttr() {

    fun columnSpan(span: Int) {
        "columnSpan" with span
    }
}

open class TableCellEvent : GroupEvent()
