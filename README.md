# AnvilCraft-DingDongJi
铁砧工艺-叮咚叽
致力于铁砧工艺护甲，工具，锻造相关的模组

## 构建

每次推送到 `main` 时，GitHub Actions 会自动用 JDK 21 编译。完成后：

- 打开仓库 **Actions** 页面，进入最近一次 **Build**，下载产物 `anvilcraft-dingdongji`
- 打标签（例如 `0.0.7` 或 `v0.0.7`）会额外发布到 **Releases**

本地：

```bat
gradlew.bat build
```

产物在 `build/libs/`。需要对照铁砧工艺源码编译时，把对应 1.6.x 的 jar 放到 `libs/`。

