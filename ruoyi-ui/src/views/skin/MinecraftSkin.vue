<template>
  <div id="minecraft-skin-container"></div>
</template>

<script>
import axios from 'axios';
import * as THREE from 'three';
import {OrbitControls} from 'three/examples/jsm/controls/OrbitControls';

export default {
  name: 'MinecraftSkin',
  data() {
    return {
      scene: null,
      camera: null,
      renderer: null,
      controls: null
    };
  },
  mounted() {
    const apiUrl = 'https://application.shenzhuo.vip/mojang/user/kissthefire';
    axios.get(apiUrl).then(response => {
      if (response.data.code === 200 && response.data.data.skin) {
        const skinUrl = response.data.data.skin.url;
        // 初始化Three.js场景、相机和渲染器
        this.scene = new THREE.Scene();
        this.camera = new THREE.PerspectiveCamera(75, window.innerWidth / window.innerHeight, 0.1, 1000);
        this.camera.position.z = 5;
        this.renderer = new THREE.WebGLRenderer();
        this.renderer.setSize(window.innerWidth, window.innerHeight);
        document.getElementById('minecraft-skin-container').appendChild(this.renderer.domElement);
        this.controls = new OrbitControls(this.camera, this.renderer.domElement);
        // 加载并渲染皮肤
        this.renderSkin(skinUrl);
        // 启动动画循环
        this.animate();
      }
    });
  },
  methods: {
    renderSkin(skinUrl) {
      const loader = new THREE.TextureLoader();
      loader.load(skinUrl, texture => {
        let geometry;
        // 根据皮肤模型类型设置不同的几何形状
        if (this.$store.state.skinModel === 'slim') {
          // 这里假设你有一个适合slim模型的几何形状定义
          geometry = new THREE.BoxGeometry(0.8, 1, 1);
        } else {
          geometry = new THREE.BoxGeometry(1, 1, 1);
        }
        const material = new THREE.MeshBasicMaterial({map: texture});
        const cube = new THREE.Mesh(geometry, material);
        this.scene.add(cube);
      });
    },
    animate() {
      requestAnimationFrame(this.animate);
      this.renderer.render(this.scene, this.camera);
      this.controls.update();
    }
  },
  beforeDestroy() {
    // 在组件销毁前清理Three.js相关资源
    if (this.renderer) {
      this.renderer.dispose();
    }
    if (this.scene) {
      this.scene.dispose();
    }
    if (this.camera) {
      this.camera = null;
    }
    if (this.controls) {
      this.controls.dispose();
    }
  }
};
</script>

<style scoped>
#minecraft-skin-container {
  width: 100%;
  height: 100vh;
  margin: 0;
  padding: 0;
  overflow: hidden;
}
</style>
