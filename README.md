# KuiklyTableView

基于 [KuiklyUI](https://github.com/Tencent-TDS/KuiklyUI) 跨端框架构建的表格组件库，提供 23 个 DSL 函数，覆盖基础表格到企业级高级功能，支持 Android、iOS、鸿蒙三端运行。

## 组件列表

| 类别 | DSL 函数 |
|------|----------|
| 基础结构 | `Table`、`HTable`（双向滚动）、`TableRow`、`TableCell` |
| 主题与排版 | `theme`、`ThemedHeaderRow`、`ThemedTableRow`、`ThemedSummaryRow` |
| 功能增强 | `SortableHeaderCell`（点击排序）、`ExpandableTableRow`（展开/折叠）、`CheckboxTableRow`（多选）、`SwipeableTableRow`（滑动操作） |
| 工具栏 | `TableSearchBar`（搜索过滤）、`PaginationBar`（分页）、`TableBatchActionBar`（批量操作）、`TableEmptyView`（空状态） |
| 布局固定 | `StickyTable`（吸顶表头）、`FrozenColumnTable`（冻结列）、`TableHeaderGroup`（合并表头）、`TableGroupCell` |
| 高级场景 | `TreeTable`（树形多级展开）、`InfiniteTable`（无限滚动加载更多）、`EditableTableRow`（内联编辑） |

## 接入指南

### 1. 添加 Maven 仓库

在 `settings.gradle.kts` 中添加：

```kotlin
dependencyResolutionManagement {
    repositories {
        maven {
            url = uri("https://mirrors.tencent.com/nexus/repository/maven-tencent/")
        }
    }
}
```

### 2. 添加依赖

```kotlin
kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("com.tencent.kuikly-open:core:2.15.0-2.0.21")
                implementation(project(":KuiklyTable"))
            }
        }
    }
}
```

## 使用示例

### 基础表格

```kotlin
Table {
    attr {
        flex(1f)
        separatorColor(Color(0xFFE0E0E0L))
        separatorHeight(0.5f)
        allowsSelection(true)
    }
    event {
        rowClick { index -> toast("Clicked row $index") }
    }
    TableRow {
        attr { rowHeight(44f); backgroundColor(Color(0xFFF5F5F5L)); flexDirectionRow() }
        TableCell { attr { flex(2f); paddingLeft(16f) }
            Text { attr { text("姓名"); fontWeightBold() } }
        }
        TableCell { attr { flex(1f); alignItemsCenter() }
            Text { attr { text("得分"); fontWeightBold() } }
        }
    }
    data.forEach { row ->
        TableRow {
            attr { rowHeight(48f); flexDirectionRow() }
            TableCell { attr { flex(2f); paddingLeft(16f) }
                Text { attr { text(row.name) } }
            }
            TableCell { attr { flex(1f); alignItemsCenter() }
                Text { attr { text(row.score.toString()) } }
            }
        }
    }
}
```

### 主题化表格

```kotlin
Table {
    attr {
        theme(TableTheme.OCEAN)
        height(360f)
    }
    ThemedHeaderRow {
        listOf("产品", "Q1", "Q2", "Q3").forEach { col ->
            SortableHeaderCell(col, ctx.sortKey == col, ctx.sortAsc) {
                ctx.sortKey = col; ctx.sortAsc = !ctx.sortAsc
            }
        }
    }
    data.forEach { item ->
        ThemedTableRow(isEven = item.index % 2 == 0) {
            TableCell { Text { attr { text(item.name) } } }
            TableCell { Text { attr { text(item.q1.toString()) } } }
        }
    }
}
```

### 树形表格

```kotlin
TreeTable {
    attr {
        columns(
            TreeTableColumn("部门", flex = 2f),
            TreeTableColumn("负责人", flex = 1f),
            TreeTableColumn("人数", flex = 1f)
        )
        nodes(orgTree)
        expandedKeys(ctx.expandedKeys)
    }
    event {
        onToggle { key -> ctx.toggleExpand(key) }
    }
}
```

### 无限滚动表格

```kotlin
InfiniteTable(
    items = ctx.items,
    loading = ctx.loading,
    hasMore = ctx.hasMore,
    onLoadMore = { ctx.loadNextPage() }
) { item ->
    TableRow {
        attr { rowHeight(52f); flexDirectionRow() }
        TableCell { Text { attr { text(item.title) } } }
    }
}
```

## 示例

完整示例见 `shared/src/commonMain/kotlin/com/kuikly/kuiklytable/TableViewDemoPage.kt`，
在线效果演示见 [GitHub Pages](https://lfan-ke.github.io/KuiklyTableView/)。

## 相关资源

- [Kuikly 官方文档](https://kuikly.tds.qq.com/)
- [KuiklyUI 仓库](https://github.com/Tencent-TDS/KuiklyUI)

## License

[KuiklyUI License](https://github.com/Tencent-TDS/KuiklyUI/blob/main/LICENSE)
