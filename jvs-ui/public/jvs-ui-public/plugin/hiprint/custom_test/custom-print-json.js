/**
 * 默认示例数据
 * */
import {printDemoData} from './print-data'
 export const demoPrintJson = {
	panels: [
		{
			index: 0,
			height: 297,
			width: 210,
			paperHeader: 45,
			paperFooter: 780,
			printElements: [
				{
					options: {
						left: 175.5,
						top: 10.5,
						height: 27,
						width: 259,
						title: "JVS Print自定义模块打印",
						fontSize: 19,
						fontWeight: "600",
						textAlign: "center",
						lineHeight: 26
					},
					printElementType: { title: "自定义文本", type: "text" }
				},
				{
					options: {
						left: 10.5,
						top: 32,
						height: 13,
						width: 52,
						title: "页眉线",
						textAlign: "center"
					},
					printElementType: { title: "自定义文本", type: "text" }
				},
				{
					options: {
						left: 10.5,
						top: 60,
						height: 48,
						width: 160,
						src: "http://www.bctools.cn/files/headlogo2.png"
					},
					printElementType: { title: "图片", type: "image" }
				},
				{
					options: {
						left: 180,
						top: 50,
						height: 19,
						width: 226,
						title: "所有打印元素都可已拖拽的方式来改变元素大小",
						fontFamily: "微软雅黑",
						textAlign: "center",
						lineHeight: 18
					},
					printElementType: { title: "自定义文本", type: "text" }
				},
				{
					options: {
						left: 180,
						top: 75,
						height: 13,
						width: 238,
						title: "单击元素，右侧可自定义元素属性",
						textAlign: "center",
						fontFamily: "微软雅黑"
					},
					printElementType: { title: "自定义文本", type: "text" }
				},
				{
					options: {
						left: 200,
						top: 95,
						height: 13,
						width: 164,
						title: "可以配置各属性的默认值",
						textAlign: "center",
						fontFamily: "微软雅黑"
					},
					printElementType: { title: "自定义文本", type: "text" }
				},
				{
					options: {
						left: 10,
						top: 120,
						height: 39,
						width: 250,
						title: "二维码以及条形码均采用svg格式打印,不同打印机打印不会造成失真,图片打印:不同DPI打印可能会导致失真",
						fontFamily: "微软雅黑",
						textAlign: "center",
						lineHeight: 18
					},
					printElementType: { title: "自定义文本", type: "text" }
				},
				{
					options: {
						left: 460,
						top: 70,
						height: 13,
						width: 120,
						title: "姓名",
						field: "name",
						testData: "jvs开发攻城狮",
						color: "#f00808",
						textDecoration: "underline",
						textAlign: "center",
						fontSize: 12
					},
					printElementType: { title: "文本", type: "text" }
				},
				{
					options: {
						left: 499.5,
						top: 100,
						height: 80,
						width: 80,
						title: "http://www.bctools.cn",
						textType: "qrcode"
					},
					printElementType: { title: "自定义文本", type: "text" }
				},
				{
					options: {
						left: 285,
						top: 125,
						height: 34,
						width: 175,
						title: "http://www.bctools.cn",
						fontFamily: "微软雅黑",
						textAlign: "center",
						textType: "barcode"
					},
					printElementType: { title: "自定义文本", type: "text" }
				},
				{
					options: {
						left: 10.5,
						top: 180,
						height: 13,
						width: 51,
						title: "横线",
						textAlign: "center"
					},
					printElementType: { title: "自定义文本", type: "text" }
				},
				{
					options: { left: 10.5, top: 195, height: 10, width: 574 },
					printElementType: { title: "横线", type: "hline" }
				},
				{
					options: {
						left: 430,
						top: 230,
						height: 150,
						width: 150,
						src: "http://knowledge.bctools.cn/jvs-knowledge-ui/static/img/code.04e9c8b0.png"
					},
					printElementType: { title: "图片", type: "image" }
				},
				{
					options: {
						left: 10.5,
						top: 210,
						height: 13,
						width: 123,
						title: "长文本会自动分页",
						textAlign: "center"
					},
					printElementType: { title: "自定义文本", type: "text" }
				},
				{
					options: {
						left: 10.5,
						top: 230,
						height: 200,
						width: 400,
						field: "longText",
						testData: printDemoData.longText
					},
					printElementType: { title: "长文", type: "longText" }
				},
				{
					options: {
						left: 10.5,
						top: 440,
						height: 13,
						width: 79,
						title: "配置项表格",
						textAlign: "center"
					},
					printElementType: { title: "自定义文本", type: "text" }
				},
				{
					options: {
						left: 109.5,
						top: 440,
						height: 13,
						width: 94,
						title: "表头列大小可拖动",
						fontFamily: "微软雅黑",
						textAlign: "center"
					},
					printElementType: { title: "自定义文本", type: "text" }
				},
				{
					options: {
						left: 220,
						top: 440,
						height: 13,
						width: 90,
						title: "红色区域可拖动",
						fontFamily: "微软雅黑",
						textAlign: "center"
					},
					printElementType: { title: "自定义文本", type: "text" }
				},
				{
					options: {
						left: 10.5,
						top: 460,
						height: 44,
						width: 561,
						field: "table",
						columns: [
							{
								columns: [
									{
										title: "商品名",
										field: "goods_name",
										width: 93.5,
										align: "center",
										colspan: 1,
										rowspan: 1
									},
									{
										title: "商品码",
										field: "goods_code",
										width: 93.5,
										align: "center",
										colspan: 1,
										rowspan: 1
									},
									{
										title: "条形码",
										field: "goods_barcode",
										width: 93.5,
										align: "center",
										colspan: 1,
										rowspan: 1
									},
									{
										title: "生产地",
										field: "location_name",
										width: 93.5,
										align: "center",
										colspan: 1,
										rowspan: 1
									},
									{
										title: "尺码",
										field: "size_name",
										width: 93.5,
										align: "center",
										colspan: 1,
										rowspan: 1
									},
									{
										title: "颜色",
										field: "color_name",
										width: 93.5,
										align: "center",
										colspan: 1,
										rowspan: 1
									}
								]
							}
						]
					},
					printElementType: { title: "表格", type: "tableCustom" }
				},
				{
					options: {
						left: 10,
						top: 520,
						height: 32,
						width: 500,
						title:
							"自定义表格：用户可左键选中表头，右键查看可操作项，操作类似Excel，双击表头单元格可进行编辑。内容：title#field",
						fontFamily: "微软雅黑",
						textAlign: "center",
						lineHeight: 15
					},
					printElementType: { title: "自定义文本", type: "text" }
				},
				{
					options: {
						left: 100,
						top: 580,
						height: 13,
						width: 342,
						title:
							"自定义模块：主要为开发人员设计，能够快速，简单，实现自己功能",
						textAlign: "center"
					},
					printElementType: { title: "自定义文本", type: "text" }
				},			
				{
					options: {
						left: 100,
						top: 600,
						height: 38,
						width: 349,
						title:
							"配置模块：主要为客户使用，开发人员可以配置属性，字段，标题等，客户直接使用",
						fontFamily: "微软雅黑",
						lineHeight: 15,
						textAlign: "center",
						color: "#d93838"
					},
					printElementType: { title: "自定义文本", type: "text" }
				},
				{
					options: { left: 15, top: 650, height: 100, width: 10 },
					printElementType: { title: "竖线", type: "vline" }
				},
				{
					options: {
						left: 22,
						top: 750,
						height: 13,
						width: 90,
						title: "竖线",
						textAlign: "center"
					},
					printElementType: { title: "自定义文本", type: "text" }
				},
				{
					options: {
						left: 130.5,
						top: 745,
						height: 10,
						width: 277
					},
					printElementType: { title: "横线", type: "hline" }
				},
				{
					options: {
						left: 210,
						top: 750,
						height: 13,
						width: 120,
						title: "横线",
						textAlign: "center"
					},
					printElementType: { title: "自定义文本", type: "text" }
				},
				{
					options: {
						left: 475.5,
						top: 640,
						height: 100,
						width: 100
					},
					printElementType: { title: "矩形", type: "rect" }
				},
				{
					options: {
						left: 476,
						top: 750,
						height: 13,
						width: 101,
						title: "矩形",
						textAlign: "center"
					},
					printElementType: { title: "自定义文本", type: "text" }
				},
				{
					options: {
						left: 525,
						top: 782,
						height: 13,
						width: 63,
						title: "页尾线",
						textAlign: "center"
					},
					printElementType: { title: "自定义文本", type: "text" }
				},
				{
					options: { left: 12, top: 786, height: 49, width: 49 },
					printElementType: { title: "html", type: "html" }
				},
				{
					options: {
						left: 200,
						top: 800,
						height: 13,
						width: 205,
						title: "页眉线已上。页尾下以下每页都会重复打印",
						textAlign: "center"
					},
					printElementType: { title: "自定义文本", type: "text" }
				}
			],
			paperNumberLeft: 565.5,
			paperNumberTop: 819
		}
	]
};
