<template>
    <div class="jvs-breadcrumb"
      :class="[{ 'jvs-breadcrumb--active': isCollapse }]"
      v-if="showCollapse"
    >
      <i class="icon-navicon"
        @click="setCollapse"></i>
    </div>
</template>
<script>
  import {mapGetters, mapState} from "vuex";
  import {fullscreenToggel, listenfullscreen} from "@/util/util";
  import topMenu from "./top-menu";

  export default {
    components: {
      topMenu
    },
    name: "top",
    data() {
      return {};
    },
    filters: {},
    created() {
    },
    mounted() {
      listenfullscreen(this.setScreen);
    },
    computed: {
      ...mapState({
        showLock: state => state.common.showLock,
        showFullScren: state => state.common.showFullScren,
        showCollapse: state => state.common.showCollapse,
        showMenu: state => state.common.showMenu,
      }),
      ...mapGetters([
        "userInfo",
        "isFullScreen",
        "tagWel",
        "tagList",
        "isCollapse",
        "tag",
        "logsLen",
        "logsFlag"
      ])
    },
    methods: {
      handleScreen() {
        fullscreenToggel();
      },
      setCollapse() {
        this.$store.commit("SET_COLLAPSE");
      },
      setScreen() {
        this.$store.commit("SET_FULLSCREN");
      },
      logout() {
        this.$confirm(`${this.$langt('common.login.outInfo')}`, `${this.$langt('common.login.tips')}`, {
          confirmButtonText: this.$langt('common.confirm'),
          cancelButtonText: this.$langt('common.cancel'),
          type: "warning"
        }).then(() => {
          this.$store.dispatch("LogOut").then(() => {
            this.$router.push({path: "/login"});
          });
        });
      }
    }
  };
</script>

<style lang="scss" scoped>
</style>
