import VerseImg from '@/assets/imgs/capture-new/Verse.png'
import SearchImg from '@/assets/imgs/capture-new/Search.png'
import CollectionImg from '@/assets/imgs/capture-new/Collection.png'
import IframeImg from '@/assets/imgs/capture-new/Iframe.png'
import TodoListImg from '@/assets/imgs/capture-new/TodoList.png'
import WeatherImg from '@/assets/imgs/capture-new/Weather.png'
import CountDownImg from '@/assets/imgs/capture-new/CountDown.png'
import JuejinListImg from '@/assets/imgs/capture-new/JuejinList.png'
import WeiboListImg from '@/assets/imgs/capture-new/WeiboList.png'
import GithubTrendingImg from '@/assets/imgs/capture-new/GithubTrending.png'
import ZhihuListImg from '@/assets/imgs/capture-new/ZhihuList.png'
import EditorImg from '@/assets/imgs/capture-new/Editor.png'
import MovieLinesImg from '@/assets/imgs/capture-new/MovieLines.png'
import BookmarkImg from '@/assets/imgs/capture-new/Bookmark.png'

export const MATERIAL_LIST_MAP = {
    StepCard: {
      label: '步骤卡片',
      text: 'test',
      img: 'EmptyImg'
    },
    ChartPie: {
      label: '饼状图',
      text: '图表-饼状图',
      img: 'EmptyImg'
    },
    Label: {
      label: '标签文本',
      text: '自定义文本信息',
      img: 'EmptyImg'
    },
    Document:{
      label: '常规文库',
      text: '以图片及文字方式展示',
      img: 'BijiImg'
    },
    Navigation:{
      label: '导航栏',
      text: '可定义导航背景及样式',
      img: 'SousuoImg'
    },
    Day: {
      label: '日期',
      text: '自定义日期格式',
      img: 'DayImg'
    },
    Clock: {
      label: '时钟',
      text: '展示当前时间信息',
      img: 'ClockImg'
    },
    Library:{
      label: '快捷文库',
      text: '可快速访问文库中文档',
      img: 'XinwenImg'
    },

    QuickCards: {
        label: '快捷项目',
        text: '可快速访问文库中文档',
        img: 'XinwenImg'
    },
    StandardCards: {
        label: '常规项目',
        text: '常规访问项目',
        img: 'XinwenImg'
    },
    // LibraryList:{
    //   label: 'LibraryList',
    //   text: '文库列表',
    //   img: TodoListImg
    // },
    Verse: {
      label: 'Verse',
      text: '随机古诗',
      img: VerseImg,
      hidden:true
    },
    Search: {
      label: 'Search',
      text: '搜索栏',
      img: SearchImg,
      hidden:true
    },
    Collection: {
      label: 'Collection',
      text: '导航收藏页',
      img: CollectionImg,
      hidden:true
    },
    Iframe: {
      label: 'Iframe',
      text: '外部网站',
      img: IframeImg,
      hidden:true
    },
    TodoList: {
      label: 'TodoList',
      text: '备忘清单',
      img: TodoListImg,
      hidden:true
    },
    Weather: {
      label: 'Weather',
      text: '天气',
      img: WeatherImg,
      hidden:true
    },
    CountDown: {
      label: 'CountDown',
      text: '倒计时',
      img: CountDownImg,
      hidden:true
    },
    JuejinList: {
      label: 'JuejinList',
      text: '掘金',
      img: JuejinListImg,
      hidden:true
    },
    WeiboList: {
      label: 'WeiboList',
      text: '微博热搜',
      img: WeiboListImg,
      hidden:true
    },
    GithubTrending: {
      label: 'GithubTrending',
      text: 'Github趋势',
      img: GithubTrendingImg,
      hidden:true
    },
    ZhihuList: {
      label: 'ZhihuList',
      text: '知乎热榜',
      img: ZhihuListImg,
      hidden:true
    },
    Editor: {
      label: 'Editor',
      text: 'Markdown写字板',
      img: EditorImg,
      hidden:true
    },
    MovieLines: {
      label: 'MovieLines',
      text: '经典电影台词',
      img: MovieLinesImg,
      hidden:true
    },
    Bookmark: {
      label: 'Bookmark',
      text: '书签导航',
      img: BookmarkImg,
      hidden:true
    }
}

export const BG_IMG_TYPE_MAP = {
    Nature: '自然',
    People: '人物',
    Architecture: '建筑',
    Technology: '科技',
    Animals: '动物'
}