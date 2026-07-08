# KuiklyTableView

基于 [KuiklyUI](https://github.com/Tencent-TDS/KuiklyUI) 跨端框架构建的表格组件，支持 Android、iOS、鸿蒙多端运行。

## 组件功能

- 支持多行多列结构（`Table` / `TableRow` / `TableCell`）
- 支持自定义分隔线颜色和高度
- 支持行选中与多选模式
- 行点击、长按、选中/取消选中事件回调
- 纯 Kotlin DSL 实现，零 native 模块依赖

## 接入指南

### 1. 添加 Maven 仓库

在 `settings.gradle.kts` 中添加仓库地址：

```kotlin
dependencyResolutionManagement {
    repositories {
        // ... 其他仓库
        maven {
            url = uri("https://mirrors.tencent.com/nexus/repository/maven-tencent/")
        }
    }
}
```

### 2. 添加依赖

在模块的 `build.gradle.kts` 中添加：

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

---

## 核心 API

### Table

表格容器，作为 `TableRow` 的父容器使用：

```kotlin
fun ViewContainer<*, *>.Table(init: TableView.() -> Unit)
```

**TableAttr 属性：**

| 方法 | 说明 |
|------|------|
| `separatorColor(Color)` | 设置行分隔线颜色 |
| `separatorHeight(Float)` | 设置行分隔线高度 |
| `allowsSelection(Boolean)` | 是否允许行选中（单选） |
| `allowsMultipleSelection(Boolean)` | 是否允许多行选中 |

**TableEvent 事件：**

| 方法 | 说明 |
|------|------|
| `rowClick { index -> }` | 行点击事件，返回行索引 |
| `rowLongPress { index -> }` | 行长按事件，返回行索引 |

---

### TableRow

表格行，须作为 `Table` 的直接子元素：

```kotlin
fun ViewContainer<*, *>.TableRow(init: TableRowView.() -> Unit)
```

**TableRowAttr 属性：**

| 方法 | 说明 |
|------|------|
| `rowHeight(Float)` | 设置行高 |
| `selectable(Boolean)` | 该行是否可选中 |

**TableRowEvent 事件：**

| 方法 | 说明 |
|------|------|
| `selected { index -> }` | 行被选中时回调 |
| `deselected { index -> }` | 行被取消选中时回调 |

---

### TableCell

表格单元格，须作为 `TableRow` 的直接子元素：

```kotlin
fun ViewContainer<*, *>.TableCell(init: TableCellView.() -> Unit)
```

**TableCellAttr 属性：**

| 方法 | 说明 |
|------|------|
| `columnSpan(Int)` | 单元格跨列数 |

---

## 使用示例

```kotlin
Table {
    attr {
        flex(1f)
        separatorColor(Color(0xFFE0E0E0L))
        separatorHeight(0.5f)
        allowsSelection(true)
    }
    event {
        rowClick { index ->
            KLog.i("Table", "row clicked: $index")
        }
    }
    // 表头行
    TableRow {
        attr {
            rowHeight(44f)
            backgroundColor(Color(0xFFF5F5F5L))
            flexDirectionRow()
        }
        TableCell {
            attr { flex(2f); justifyContentCenter(); paddingLeft(16f) }
            Text {
                attr { fontSize(14f); fontWeight700(); color(Color(0xFF333333L)); text("Name") }
            }
        }
        TableCell {
            attr { flex(1f); justifyContentCenter(); alignItemsCenter() }
            Text {
                attr { fontSize(14f); fontWeight700(); color(Color(0xFF333333L)); text("Score") }
            }
        }
    }
    // 数据行
    TableRow {
        attr {
            rowHeight(48f)
            flexDirectionRow()
        }
        event {
            selected { idx -> KLog.i("Table", "row $idx selected") }
            deselected { idx -> KLog.i("Table", "row $idx deselected") }
        }
        TableCell {
            attr { flex(2f); justifyContentCenter(); paddingLeft(16f) }
            Text { attr { fontSize(14f); color(Color(0xFF212121L)); text("Alice") } }
        }
        TableCell {
            attr { flex(1f); justifyContentCenter(); alignItemsCenter() }
            Text { attr { fontSize(14f); color(Color(0xFF212121L)); text("95") } }
        }
    }
}
```

---

## 项目结构

```
KuiklyTableView/
├── KuiklyTable/          - 表格组件库模块（KMP）
│   └── src/commonMain/kotlin/com/tencent/kuiklybase/table/
│       └── TableView.kt  - Table / TableRow / TableCell 组件实现
├── shared/               - Demo KMP 模块
│   └── src/commonMain/kotlin/com/kuikly/kuiklytable/
│       └── TableViewDemoPage.kt  - Demo 页面（@Page 注册）
├── androidApp/           - Android Demo Runner
├── iosApp/               - iOS Demo Runner
└── ohosApp/              - HarmonyOS Demo Runner
```

---

## License

[KuiklyUI License](https://github.com/Tencent-TDS/KuiklyUI/blob/main/LICENSE)
