<template>
  <div :class="{'calendar-box': true, }">
    <div :class="{'with-title-content': (componentSetting.name || componentSetting.titleTxt)}">
      <div class="mc-tool">
        <i class="el-icon-d-arrow-left" @click="calendarClick('last', 'year')"></i>
        <div class="center">
          <i class="el-icon-arrow-left" @click="calendarClick('last', 'month')"></i>
          <span>{{getDay(calenderDay)}}</span>
          <i class="el-icon-arrow-right" @click="calendarClick('next', 'month')"></i>
        </div>
        <i class="el-icon-d-arrow-right" @click="calendarClick('next', 'year')"></i>
      </div>
      <div class="my-calendar">
        <date-table
          selection-mode="day"
          :first-day-of-week="7"
          :value="calenderDay"
          :default-value="currentDay"
          :date="calenderDay"
          @pick="handleDatePick">
        </date-table>
      </div>
    </div>
  </div>
</template>

<script>
import DateTable from 'element-ui/packages/date-picker/src/basic/date-table'
export default {
  name: "Calendar",
  components: {
    DateTable
  },
  props: {
    element: {
      type: Object
    },
    componentSetting: {
      type: Object,
      default() {
        return {}
      }
    },
    options: {
      type: Object,
      default() {
        return {}
      }
    },
  },
  
  data() {
    return {
      currentDay: new Date(),
      calenderDay: new Date(),
    }
  },
  methods: {
    handleDatePick (value) {
      this.calenderDay = value
    },
    getDay (date) {
      return date.getFullYear() + '-' + ((date.getMonth()+1) > 9 ? (date.getMonth()+1) : ('0'+(date.getMonth()+1))) + '-' + (date.getDate() > 9 ? date.getDate() : ('0'+date.getDate()))
    },
    calendarClick (type, unit) {
      let y = this.calenderDay.getFullYear()
      let m = this.calenderDay.getMonth() + 1
      let d = this.calenderDay.getDate()
      if(unit == 'year') {
        if(type == 'last') {
          y -= 1
        }
        if(type == 'next') {
          y += 1
        }
      }
      if(unit == 'month') {
        if(type == 'last') {
          if(m > 1) {
            m -= 1
          }else{
            m = 12
            y -= 1
          }
        }
        if(type == 'next') {
          if(m < 11) {
            m += 1
          }else{
            m = 1
            y += 1
          }
        }
      }
      this.calenderDay = new Date(y+'-'+m+'-'+d)
    },
  }
}
</script>

<style lang="scss" scoped>
.calendar-box{
  width: 100%;
  height: 100%;
  padding: 0 24px;
  box-sizing: border-box;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  justify-content: center;
  .with-title-content{
    width: 100%;
    height: 100%;
  }
  .my-calendar, .mc-tool{
    display: flex;
    align-items: center;
    justify-content: center;
    /deep/.el-date-table{
      width: 100%;
    }
  }
  .mc-tool{
    margin-bottom: 16px;
    font-size: 14px;
    width: 100%;
    box-sizing: border-box;
    .center{
      text-align: center;
      flex: 1;
      display: flex;
      align-items: center;
      justify-content: center;
      span{
        margin: 0 16px;
      }
    }
    i{
      color: #6F7588;
      font-weight: bold;
      padding: 5px;
      border-radius: 50%;
      overflow: hidden;
      cursor: pointer;
      &:hover{
        color: #1E6FFF;
        background-color: #f1f4fd;
      }
    }
  }
  .my-calendar{
    /deep/.el-date-table{
      tr:not(.el-date-table__row){
        height: 40px;
        border-radius: 2px;
        overflow: hidden;
      }
      th, td{
        font-family: Source Han Sans-Regular, Source Han Sans;
        font-weight: 400;
        font-size: 14px;
        color: #363B4C;
      }
      .el-date-table__row{
        td{
          padding: 8px 0 0 0;
          div{
            padding: 0;
            height: 36px;
            span{
              width: 36px;
              height: 36px;
              line-height: 36px;
            }
          }
          &.today{
            div{
              span{
                color: #fff;
                background: #1E6FFF;
                box-shadow: 0px 4px 15px 0px rgba(30,111,255,0.3);
                border-radius: 4px;
              }
            }
          }
          &.current:not(.disabled){
            span{
              color: #fff;
              background: #1E6FFF;
              border-radius: 4px;
            }
          }
          &.prev-month, &.next-month{
            color: #6F7588;
          }
        }
      }
    }
  }
}
</style>
