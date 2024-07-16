/*
 Navicat Premium Data Transfer

 Source Server         : 本地-mysql-糖尿病医院电脑
 Source Server Type    : MySQL
 Source Server Version : 80011
 Source Host           : localhost:3306
 Source Schema         : db_lock

 Target Server Type    : MySQL
 Target Server Version : 80011
 File Encoding         : 65001

 Date: 16/07/2024 09:41:11
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for device
-- ----------------------------
DROP TABLE IF EXISTS `device`;
CREATE TABLE `device`  (
  `id` bigint(20) NOT NULL COMMENT '主键',
  `use_times` int(10) NULL DEFAULT 0 COMMENT '使用时长',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of device
-- ----------------------------
INSERT INTO `device` VALUES (1, 0);
INSERT INTO `device` VALUES (2, 0);
INSERT INTO `device` VALUES (3, 0);
INSERT INTO `device` VALUES (4, 0);

SET FOREIGN_KEY_CHECKS = 1;
