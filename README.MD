### 优点
目前移动端编辑器的实现采用了H5方案，其优点是NA双端格式统一、编辑内容所见即所得并方便在PC端展示，缺点是维护过程需要前端人力，且由于编辑器与NA交互时采用了jsBridge方案，因此同时需要移动端跟进维护并与前端同学沟通。

采用Native方案实现富文本编辑器可降低人力成本，并可实现多格式高性能图片预览、视频浏览、自定义其他格式文件等功能。
通过反编译、检查层级等方法调研了市场上具有编辑器功能的app后发现：WPS、知乎、印象笔记采用H5方案，锤子便签采用NA方案。Github上富文本类项目，关注度高的大多采用H5方案，其余使用NA方案的项目bug多、功能少，无法支撑商业级App项目。

### 原理
由于原生的 `BulletSpan`、`QuoteSpan`、`DrawableMarginSpan` 在多行模式下，光标位置都会有错位问题，不适合用单一EditText来实现有序无序列表、引用等格式，因此采用了多个EditText组合的方式实现多种富文本样式。
同时提供了自定义样式的接口，方便新增、修改当前已有的富文本样式。

### 功能
目前具有加粗、斜体、中划线、下划线、标题、引用、有序列表、无序列表、链接、分隔线，并可以插入图片、视频、音频、云盘等任意类型资源，同时具有转为HTML、转为JSON、从JSON还原数据的功能，接口设计遵循用户友好的原则，可自定义插入任意类型的资源。整个项目遵循高内聚低耦合原则，用户可自定义数据格式转换adapter。
目前项目实现了约20种编辑功能，100+项编辑规则。

### 效果

<img src="https://github.com/Carbs0126/Screenshot/blob/master/wricheditor_0.jpeg" width="350" height="780"/><br>

<img src="https://github.com/Carbs0126/Screenshot/blob/master/wricheditor_1.jpeg" width="350" height="780"/><br>


### 使用与集成
编辑器只需要集成`WRichEditor`即可，使用方式：

1.先将`library`中的代码集成到项目中，推荐新建一个`module`，然后将`library`的代码拷贝到此`module`内。

2.在需要使用编辑器的界面对应的`layout`文件中，声明`WRichEditor`：
``` xml
<cn.carbs.wricheditor.library.WRichEditor
    android:id="@+id/wrich_editor_view"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:layout_below="@id/title_bar"
    android:layout_marginLeft="16dp"
    android:layout_marginRight="16dp"
    android:clipToPadding="false"
    android:paddingBottom="42dp"
    app:editorColor="#FF513930"
    app:headlineTextSize="18dp"
    app:linkColor="#FF2196F3"
    app:scrollBottomDeltaY="80dp" />
```  

3. `WRichEditor`的编辑功能可分为两大类：编辑文字格式以及插入资源。这两种操作的处理方式有较大不同，表现在API层面可见示例中的`MainActivity.java`文件。由于内部已经进行了封装，屏蔽了绝大多数涉及到格式切换等复杂问题，因此改变文字格式的方式非常简单：
```java
    private void onBoldClicked() {
        Set<RichType> richTypes = mWRichEditor.getRichTypes();
        boolean open = TypeUtil.toggleCertainRichType(richTypes, RichType.BOLD);
        mWRichEditor.updateTextByRichTypeChanged(RichType.BOLD, open, null);
        setButtonTextColor(mBtnBold, open);
    }

    private void onItalicClicked() {
        Set<RichType> richTypes = mWRichEditor.getRichTypes();
        boolean open = TypeUtil.toggleCertainRichType(richTypes, RichType.ITALIC);
        mWRichEditor.updateTextByRichTypeChanged(RichType.ITALIC, open, null);
        setButtonTextColor(mBtnItalic, open);
    }

    private void onStrikeThroughClicked() {
        Set<RichType> richTypes = mWRichEditor.getRichTypes();
        boolean open = TypeUtil.toggleCertainRichType(richTypes, RichType.STRIKE_THROUGH);
        mWRichEditor.updateTextByRichTypeChanged(RichType.STRIKE_THROUGH, open, null);
        setButtonTextColor(mBtnStrikeThrough, open);
    }

    private void onUnderLineClicked() {
        Set<RichType> richTypes = mWRichEditor.getRichTypes();
        boolean open = TypeUtil.toggleCertainRichType(richTypes, RichType.UNDER_LINE);
        mWRichEditor.updateTextByRichTypeChanged(RichType.UNDER_LINE, open, null);
        setButtonTextColor(mBtnUnderLine, open);
    }

    private void onHeadLineClicked() {
        Set<RichType> richTypes = mWRichEditor.getRichTypes();
        boolean open = TypeUtil.toggleCertainRichType(richTypes, RichType.HEADLINE);
        mWRichEditor.updateTextByRichTypeChanged(RichType.HEADLINE, open, null);
        setButtonTextColor(mBtnHeadline, open);
    }
    private void onInsertQuoteClicked() {
        Set<RichType> richTypes = mWRichEditor.getRichTypes();
        boolean open = TypeUtil.toggleCertainRichType(richTypes, RichType.QUOTE);
        mWRichEditor.updateTextByRichTypeChanged(RichType.QUOTE, open, null);
        setButtonTextColor(mBtnQuote, open);
    }
```
富文本编辑器的另一种强大功能表现在可以插入自定义的资源内容，如插入图片、音频、视频、网盘资源、名片等等，只需要实现`IRichCellView`接口，同时定义一个此`Cell`中使用到的`Data`（即实现`IRichCellData`），可参考示例中`library`中的`RichImageView`以及主工程自定义的`MyRichImageView`。

在设计API时遵从外部使用者的思维习惯，外部插入自定义的资源视图与数据时，只需要新增 "遵从`IRichCellView`的自定义`View`" + "遵从`IRichCellData`的自定义`Data`"，即可插入一条资源。同时当资源融入富文本时，内部会自动调整为符合富文本编辑的格式
```java
    private void onInsertImageClicked() {
        MyRichImageView richImageView = new MyRichImageView(MainActivity.this);
        ImageCellData imageCellData = new ImageCellData();
        imageCellData.imageNetUrl = "https://ss2.bdstatic.com/70cFvnSh_Q1YnxGkpoWK1HF6hhy/it/u=1284023848,585277846&fm=11&gp=0.jpg";
        richImageView.setCellData(imageCellData);
        mWRichEditor.insertImage(richImageView);
    }
```
3. 导出功能。此富文本编辑器提供了两种导出功能：导出为`HTML`和导出为`json`，其中导出的`json`可以还原富文本

导出为`html`：
```java
String out = ParserUtil.parseToHtml(mWRichEditor).toString();
```
导出为`json`：
```java
String out = ParserUtil.parseToJson(mWRichEditor).toString();
```
4. 还原功能，从`json`还原：(还原时，如果自定义了对应的资源视图，需要为`CustomViewProvider`提供指定的type与自定义资源View的转换关系)
```java
private void inflateByJson() {
    ParserUtil.inflateFromJson(this, mWRichEditor, mJSON, new CustomViewProvider() {
        @Override
        public IRichCellView getCellViewByRichType(RichType richType) {
            if (richType == RichType.IMAGE) {
                return new MyRichImageView(RestoreActivity.this);
            }
            return null;
        }
    });
}
```




### TODO
1. 使用HTML还原富文本
2. 如果editor是第一个，删除时，应该将 LineFormat 删除掉
3. ScrollView中的第0位置，添加自定义的HeaderView
4. 有序无序列表间距与普通文字的行间距不同
5. headline偶现失效
6. 换行后cursor问题
7. 相同格式的wrappermerge一下
8. 暂未上传bintray库

### EXPERIENCE
1. 锤子便签：在quote时，会自动将所有的字符进行一次缩小，颜色变浅
2. 换肤、主题、文字颜色
3. MarkDown 格式
4. 锤子便签：待办事项
5. RichVideoView RichAudioView
6. 暂时不支持HTML标签
7. 本地存储+本地还原
8. HTML存储+HTML还原

### FIXED
1. 选中某段文字后，点击某个格式，只会响应此变化的格式，之前已经选中的格式不能带入
2. 检查delete键是否连续，image等类型
3. 首行改为Quote模式时，下一行不增加cell的问题
4. ordered list 的体验增强，修改 needAddWRichEditor 方法
5. 点击ScrollView的空白处，将焦点至于最后一个Editor中
6. 点击某个Editor时，被软键盘遮挡后自动调整位置

## Contact
wechat: `AutoReleasePool`

## License

    Copyright 2016 Carbs.Wang (WRichEditor-Android)

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
