<template>
  <div :key="fresh">
    <el-table-column
      v-for="md in MonDayList"
      :key="md.value"
      class-name="gantt-column-father"
    >
      <template slot="header" slot-scope="scope">
        <div class="gantt-header mon">
          <!-- <div>{{md.label}}</div> -->
          <div>{{ md.value }}</div>
          <!-- <div>
            {{ new Date(md.value).getFullYear() }}.{{
              new Date(md.value).getMonth() + 1 < 10
                ? `0${new Date(md.value).getMonth() + 1}`
                : new Date(md.value).getMonth() + 1
            }}
          </div> -->
        </div>
      </template>
      <el-table-column
        v-for="day in md.children"
        :key="day.value"
        :width="option.showType === 'day' ? 20 : 30"
        class-name="gantt-column"
      >
        <template slot="header" slot-scope="scope">
          <div class="gantt-header day">
            <div>{{ day.label }}</div>
          </div>
        </template>
        <template slot-scope="scope">
          <div
            class="gantt-back"
            :style="`background: ${getStyle(scope.row, day, 'back')};`"
          >
            <div
              class="gantt-bar"
              :style="`background: ${getStyle(scope.row, day, 'bar')};`"
            >
              <!-- 只有实际开始时间不为空时，才在实际进度上展示百分比 -->
              <!-- <div
                  v-if="shouldShowPercentage(scope.row, day)"
                  class="gantt-percentage"
                >
                  {{ scope.row[option.actualPlanPercent] }}%
                </div> -->
              <el-popover
                v-if="shouldShowPercentage(scope.row, day)"
                v-model="visible"
                popper-class="gantt-percent-popover"
                trigger="manual"
                :content="`${scope.row[option.actualPlanPercent]}%`"
              >
              </el-popover>
            </div>
          </div>
        </template>
      </el-table-column>
    </el-table-column>
  </div>
</template>
<script>
import { dateFormat } from "@/util/date";
export default {
  name: "ganttHeader",
  props: {
    fromView: {
      type: Boolean,
    },
    option: {
      type: Object,
    },
    row: {
      type: Object,
    },
    index: {
      type: [String, Number],
    },
    min: {
      type: [String, Date],
    },
    max: {
      type: [String, Date],
    },
    fresh: {
      type: Boolean,
    },
  },
  data() {
    return {
      minDate: "",
      maxDate: "",
      MonDayList: [],
    };
  },
  created() {
    console.log("ganttOption", this.option);
    if (this.fromView) {
      this.init();
    }
  },
  methods: {
    init() {
      let now = new Date();
      if (this.min) {
        this.minDate = dateFormat(this.min, "yyyy-MM-dd");
      } else {
        this.minDate = dateFormat(
          new Date(now.getTime() - 10 * 24 * 60 * 60 * 1000),
          "yyyy-MM-dd"
        );
      }
      if (this.max) {
        this.maxDate = dateFormat(this.max, "yyyy-MM-dd");
      } else {
        this.maxDate = dateFormat(
          new Date(now.getTime() + 10 * 24 * 60 * 60 * 1000),
          "yyyy-MM-dd"
        );
      }
      if (new Date(this.minDate) > new Date(this.maxDate)) {
        let tp = this.minDate;
        this.minDate = this.maxDate;
        this.maxDate = tp;
      }
      this.getMonDayList(this.minDate, this.maxDate);
    },
    // 动态表格列
    getMonDayList(min, max) {
      // console.log(min, max);
      let minD = new Date(min);
      let maxD = new Date(max);
      let list = [];
      if (this.option.showType === "day") {
        // 按天展示
        if (minD.getFullYear() == maxD.getFullYear()) {
          // 同年
          let minM = minD.getMonth() + 1;
          let maxM = maxD.getMonth() + 1;
          if (minM == maxM) {
            let obj = {
              label: minD.getFullYear() + "年" + minM + "月",
              value: minD.getFullYear() + "-" + (minM > 9 ? minM : `0${minM}`),
              children: [],
            };
            let mind = minD.getDate();
            let maxd = maxD.getDate();
            for (let i = mind; i <= maxd; i++) {
              obj.children.push({
                label: i < 10 ? `0${i}` : i,
                value: obj.value + "-" + (i < 10 ? `0${i}` : i),
              });
            }
            list.push(obj);
          } else {
            for (let i = minM; i <= maxM; i++) {
              let obj = {
                label: minD.getFullYear() + "年" + i + "月",
                value: minD.getFullYear() + "-" + (i > 9 ? i : `0${i}`),
                children: [],
              };
              let mind = 1;
              let maxd = 30;
              if ([1, 3, 5, 7, 8, 10, 12].indexOf(i) > -1) {
                maxd = 31;
              } else {
                if (i == 2) {
                  if (minD.getFullYear() % 4 == 0) {
                    maxd = 29;
                  } else {
                    maxd = 28;
                  }
                }
              }
              if (i == minM) {
                mind = minD.getDate();
              } else if (i == maxM) {
                maxd = maxD.getDate();
              }
              for (let j = mind; j <= maxd; j++) {
                obj.children.push({
                  label: j < 10 ? `0${j}` : j,
                  value: obj.value + "-" + (j < 10 ? `0${j}` : j),
                });
              }
              list.push(obj);
            }
          }
        } else {
          // 跨年
          let minY = minD.getFullYear();
          let maxY = maxD.getFullYear();
          for (let i = minY; i <= maxY; i++) {
            let minM = 1;
            let maxM = 12;
            if (i == minY) {
              minM = minD.getMonth() + 1;
            } else if (i == maxY) {
              maxM = maxD.getMonth() + 1;
            }
            for (let j = minM; j <= maxM; j++) {
              let obj = {
                label: i + "年" + j + "月",
                value: i + "-" + (j > 9 ? j : `0${j}`),
                children: [],
              };
              let mind = 1;
              let maxd = 30;
              if ([1, 3, 5, 7, 8, 10, 12].indexOf(j) > -1) {
                maxd = 31;
              } else {
                if (j == 2) {
                  if (i % 4 == 0) {
                    maxd = 29;
                  } else {
                    maxd = 28;
                  }
                }
              }
              if (i == minY && j == minD.getMonth() + 1) {
                mind = minD.getDate();
              }
              if (i == maxY && j == maxD.getMonth() + 1) {
                maxd = maxD.getDate();
              }
              for (let k = mind; k <= maxd; k++) {
                obj.children.push({
                  label: k < 10 ? `0${k}` : k,
                  value: obj.value + "-" + (k < 10 ? `0${k}` : k),
                });
              }
              list.push(obj);
            }
          }
        }
      } else {
        // 按月展示
        let minY = minD.getFullYear();
        let maxY = maxD.getFullYear();
        let minM, maxM;
        for (let i = minY; i <= maxY; i++) {
          let obj = {
            label: i + "年",
            value: i,
            children: [],
          };
          list.push(obj);
          if (i === minY) {
            minM = minD.getMonth() + 1;
            maxM = 12;
          } else if (i === maxY) {
            minM = 1;
            maxM = maxD.getMonth() + 1;
          } else {
            minM = 1;
            maxM = 12;
          }
          for (let j = minM; j <= maxM; j++) {
            obj.children.push({
              label: j,
              value: `${i}-${j < 10 ? `0${j}` : j}`,
            });
          }
        }
      }
      this.MonDayList = list;
      console.log("MonDayList", this.MonDayList);
    },
    // 单元格动态颜色
    getStyle(row, day, type) {
      let back = "";
      let color = "";
      let dayTime = new Date(day.value).getTime();
      let plainStartTime = "";
      let plainEndTime = "";
      let reallyStartTime = "";
      let reallyEndTime = "";
      let nowTime = new Date(dateFormat(new Date(), "yyyy-MM-dd")).getTime();
      // console.log(row);
      if (row[this.option.plainStart] && row[this.option.plainEnd]) {
        if (row[this.option.plainStart]) {
          plainStartTime =
            this.option.showType === "day"
              ? new Date(row[this.option.plainStart]).getTime()
              : new Date(row[this.option.plainStart].substring(0, 7)).getTime();
        }
        if (row[this.option.plainEnd]) {
          plainEndTime =
            this.option.showType === "day"
              ? new Date(row[this.option.plainEnd]).getTime()
              : new Date(row[this.option.plainEnd].substring(0, 7)).getTime();
        }
        if (row[this.option.reallyStart]) {
          reallyStartTime =
            this.option.showType === "day"
              ? new Date(row[this.option.reallyStart]).getTime()
              : new Date(
                  row[this.option.reallyStart].substring(0, 7)
                ).getTime();
        }
        if (row[this.option.reallyEnd]) {
          reallyEndTime =
            this.option.showType === "day"
              ? new Date(row[this.option.reallyEnd]).getTime()
              : new Date(row[this.option.reallyEnd].substring(0, 7)).getTime();
        }
        /* 默认颜色 */
        if (
          plainStartTime &&
          !(dayTime < plainStartTime) &&
          plainEndTime &&
          !(dayTime > plainEndTime)
        ) {
          back = this.option.plainColor || "#EBEEF5";
        }
        if (reallyStartTime && !(dayTime < reallyStartTime)) {
          if (reallyEndTime) {
            if (!(dayTime > reallyEndTime)) {
              color = this.option.reallyColor || "#909399";
            }
          } else {
            if (!(dayTime > nowTime)) {
              color = this.option.reallyColor || "#909399";
            }
          }
        }
        /* 动态颜色 */
        if (
          (back || color) &&
          this.option.conditionControlEnable &&
          this.option.conditionControl &&
          this.option.conditionControl.length > 0
        ) {
          for (let i in this.option.conditionControl) {
            let vals = this.option.conditionControl[i].value
              ? this.option.conditionControl[i].value.split(",")
              : [];
            if (
              vals.indexOf(row[this.option.conditionControl[i].key] + "") > -1
            ) {
              if (back && this.option.conditionControl[i].plainColor) {
                back = this.option.conditionControl[i].plainColor;
              }
              if (color && this.option.conditionControl[i].reallyColor) {
                color = this.option.conditionControl[i].reallyColor;
              }
              break;
            }
          }
        }
        // // 圆角
        // if(back) {
        //   // 最早
        //   if(this.MonDayList.length > 0 && this.MonDayList[0].children && this.MonDayList[0].children.length > 0) {
        //     if(new Date(row[this.option.plainStart]) > new Date(this.MonDayList[0].children[0].value)) {
        //       if(row[this.option.plainStart] == day.value) {
        //         back += ';border-radius: 4px 0 0 4px'
        //       }
        //     }else{
        //       if(day.value == this.MonDayList[0].children[0].value) {
        //         back += ';border-radius: 4px 0 0 4px'
        //       }
        //     }
        //   }
        //   // 最晚
        //   if(this.MonDayList.length > 0 && this.MonDayList[this.MonDayList.length-1].children && this.MonDayList[this.MonDayList.length-1].children.length > 0) {
        //     if(new Date(row[this.option.plainEnd]) > new Date(this.MonDayList[this.MonDayList.length-1].children[this.MonDayList[this.MonDayList.length-1].children.length-1].value)) {
        //       if(day.value == this.MonDayList[this.MonDayList.length-1].children[this.MonDayList[this.MonDayList.length-1].children.length-1].value) {
        //         back += ';border-radius: 0 4px 4px 0'
        //       }
        //     }else{
        //       if(row[this.option.plainEnd] == day.value) {
        //         back += ';border-radius: 0 4px 4px 0'
        //       }
        //     }
        //   }
        // }
      }
      if (type == "back") {
        return back;
      }
      if (type == "bar") {
        return color;
      }
    },
    // 是否展示百分比
    shouldShowPercentage(row, day) {
      // console.log(row, day);
      const reallyStartValue = row[this.option.reallyStart];
      if (
        !this.option.reallyStart ||
        !reallyStartValue ||
        reallyStartValue === "" ||
        reallyStartValue === "-"
      ) {
        return false;
      }
      const dayValue = day.value;
      let percentageDateStr = "";
      const reallyEndValue = row[this.option.reallyEnd];
      if (reallyEndValue && reallyEndValue !== "" && reallyEndValue !== "-") {
        percentageDateStr =
          this.option.showType === "day"
            ? dateFormat(new Date(reallyEndValue), "yyyy-MM-dd")
            : dateFormat(new Date(), "yyyy-MM");
      } else {
        percentageDateStr =
          this.option.showType === "day"
            ? dateFormat(new Date(), "yyyy-MM-dd")
            : dateFormat(new Date(), "yyyy-MM");
      }
      return dayValue === percentageDateStr;
    },
  },
  watch: {
    fresh: {
      handler(newVal, oldVal) {
        if (!newVal) {
          this.init();
        }
      },
    },
  },
};
</script>
<style lang="scss" scoped>
.gantt-header {
  text-align: center;
  &.day {
    font-family: Source Han Sans-Regular, Source Han Sans;
    font-weight: 400;
    font-size: 8px;
    color: #363b4c;
  }
  &.mon {
    word-break: keep-all;
    font-size: 10px;
  }
}
.gantt-back {
  width: 100%;
  overflow: hidden;
  .gantt-bar {
    width: 100%;
    height: 23px;
  }
}
</style>
<style lang="scss">
.gantt-percent-popover {
  width: max-content;
  min-width: auto;
  border: none;
  box-shadow: none;
  background: none;
  padding: 0 !important;
  font-size: 14px;
  color: rgba(0, 0, 0, 1);
  font-weight: bold;
  // text-shadow: 1px 1px 1px rgba(0, 0, 0, 0.5);
  white-space: nowrap;
  height: 23px;
  line-height: 23px;
  right: 2px;
  font-style: italic;
  z-index: 3; // 保证比固定列的z-index小
}
</style>
