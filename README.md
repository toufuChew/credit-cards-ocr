# credit-cards-ocr
可用于手机扫描识别银行卡号的应用，算法主要原理是根据计算机视觉处理方法，区分凹凸卡及平面卡类型，针对凹凸卡处理效果不佳进行了针对性优化。
细节讲解请参考系列文章 [《Java + OpenCV 实现银行卡号识别》](https://www.jianshu.com/p/94db63562b47)
## 卡号定位
卡号排列方式主要有两种，16位4分制和其他。后期对卡号区域定位效果可观，准确率90%左右。
## 数字分割
分割对进行了深度处理的凹凸卡采用先切分粘合部位，再进行聚合的方式；而对平面卡可直接使用普通方法划分。
## ocr
ocr部分，为了方便这里用了tesseract进行识别，使用专门训练好的银行卡训练集来识别划分出来的数字。
## 效果
参考 CardOCR


