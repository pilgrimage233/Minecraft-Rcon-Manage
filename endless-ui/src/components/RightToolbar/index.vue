<template>
  <div :style="style" class="top-right-btn">
    <el-row>
      <el-tooltip v-if="search" :content="showSearch ? '隐藏搜索' : '显示搜索'" class="item" effect="dark"
                  placement="top">
        <el-button circle icon="el-icon-search" size="mini" @click="toggleSearch()"/>
      </el-tooltip>
      <el-tooltip class="item" content="刷新" effect="dark" placement="top">
        <el-button circle icon="el-icon-refresh" size="mini" @click="refresh()"/>
      </el-tooltip>
      <el-tooltip v-if="columns" class="item" content="显隐列" effect="dark" placement="top">
        <el-button v-if="showColumnsType == 'transfer'" circle icon="el-icon-menu" size="mini" @click="showColumn()"/>
        <el-dropdown v-if="showColumnsType == 'checkbox'" :hide-on-click="false" style="padding-left: 12px"
                     trigger="click">
          <el-button circle icon="el-icon-menu" size="mini"/>
          <el-dropdown-menu slot="dropdown">
            <template v-for="item in columns">
              <el-dropdown-item :key="item.key">
                <el-checkbox :checked="item.visible" :label="item.label" @change="checkboxChange($event, item.label)"/>
              </el-dropdown-item>
            </template>
          </el-dropdown-menu>
        </el-dropdown>
      </el-tooltip>
    </el-row>
    <el-dialog :title="title" :visible.sync="open" append-to-body>
      <el-transfer
        v-model="value"
        :data="columns"
        :titles="['显示', '隐藏']"
        @change="dataChange"
      ></el-transfer>
    </el-dialog>
  </div>
</template>
<script>
export default {
  name: "RightToolbar",
  data() {
    return {
      // 显隐数据
      value: [],
      // 弹出层标题
      title: "显示/隐藏",
      // 是否显示弹出层
      open: false,
    };
  },
  props: {
    /* 是否显示检索条件 */
    showSearch: {
      type: Boolean,
      default: true,
    },
    /* 显隐列信息 */
    columns: {
      type: Array,
    },
    /* 是否显示检索图标 */
    search: {
      type: Boolean,
      default: true,
    },
    /* 显隐列类型（transfer穿梭框、checkbox复选框） */
    showColumnsType: {
      type: String,
      default: "checkbox",
    },
    /* 右外边距 */
    gutter: {
      type: Number,
      default: 10,
    },
  },
  computed: {
    style() {
      const ret = {};
      if (this.gutter) {
        ret.marginRight = `${this.gutter / 2}px`;
      }
      return ret;
    }
  },
  created() {
    if (this.showColumnsType == 'transfer') {
      // 显隐列初始默认隐藏列
      for (let item in this.columns) {
        if (this.columns[item].visible === false) {
          this.value.push(parseInt(item));
        }
      }
    }
  },
  methods: {
    // 搜索
    toggleSearch() {
      this.$emit("update:showSearch", !this.showSearch);
    },
    // 刷新
    refresh() {
      this.$emit("queryTable");
    },
    // 右侧列表元素变化
    dataChange(data) {
      for (let item in this.columns) {
        const key = this.columns[item].key;
        this.columns[item].visible = !data.includes(key);
      }
    },
    // 打开显隐列dialog
    showColumn() {
      this.open = true;
    },
    // 勾选
    checkboxChange(event, label) {
      this.columns.filter(item => item.label == label)[0].visible = event;
    }
  },
};
</script>
<style lang="scss" scoped>
::v-deep .el-transfer__button {
  border-radius: 50%;
  padding: 12px;
  display: block;
  margin-left: 0px;
}

::v-deep .el-transfer__button:first-child {
  margin-bottom: 10px;
}
</style>
