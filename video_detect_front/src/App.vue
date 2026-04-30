<template>
  <div id="app">
    <router-view v-if="isInitialized" />
  </div>
</template>

<script>
export default {
  name: 'App',
  data() {
    return {
      isInitialized: false
    };
  },
  created() {
    this.isInitialized = true; // 避免 Vuex 数据未加载时出现问题
  }
};
const debounce = (fn, delay) => {
  let timer = null;
  return function () {
    let context = this;
    let args = arguments;
    clearTimeout(timer);
    timer = setTimeout(function () {
      fn.apply(context, args);
    }, delay);
  }
}

const _ResizeObserver = window.ResizeObserver;
window.ResizeObserver = class ResizeObserver extends _ResizeObserver{
  constructor(callback) {
    callback = debounce(callback, 16);
    super(callback);
  }
}


</script>

<style>
/* 确保根元素占满整个视口高度 */
html,
body,
#app {
  height: 100%;
  margin: 0;
  padding: 0;
}
</style>