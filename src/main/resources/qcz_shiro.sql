/*
Navicat MySQL Data Transfer

Source Server         : localhost
Source Server Version : 50726
Source Host           : 127.0.0.1:3306
Source Database       : qsnbm

Target Server Type    : MYSQL
Target Server Version : 50726
File Encoding         : 65001

Date: 2020-02-02 14:25:40
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for mt_permission
-- ----------------------------
DROP TABLE IF EXISTS `mt_permission`;
CREATE TABLE `mt_permission` (
  `pid` smallint(2) NOT NULL AUTO_INCREMENT COMMENT '权限ID',
  `perm_exp` varchar(100) NOT NULL DEFAULT '' COMMENT '权限表达式(xxx:xxx/xxx:xxx:xxx)',
  `remark` varchar(50) DEFAULT '' COMMENT '权限备注',
  `status` tinyint(1) NOT NULL DEFAULT '1' COMMENT '权限状态，0 -> 无效，1 -> 正常',
  PRIMARY KEY (`pid`),
  UNIQUE KEY `permission_pid` (`pid`) USING BTREE,
  UNIQUE KEY `permission_perm_exp` (`perm_exp`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Table structure for mt_role
-- ----------------------------
DROP TABLE IF EXISTS `mt_role`;
CREATE TABLE `mt_role` (
  `rid` smallint(2) NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  `role_name` varchar(30) NOT NULL DEFAULT '' COMMENT '角色名',
  `remark` varchar(50) DEFAULT '' COMMENT '角色备注',
  `status` tinyint(1) NOT NULL DEFAULT '1' COMMENT '角色状态，0 -> 无效，1 -> 正常',
  PRIMARY KEY (`rid`),
  UNIQUE KEY `role_name` (`role_name`) USING BTREE,
  UNIQUE KEY `rid` (`rid`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Table structure for mt_role_permission
-- ----------------------------
DROP TABLE IF EXISTS `mt_role_permission`;
CREATE TABLE `mt_role_permission` (
  `rid` smallint(2) NOT NULL DEFAULT '0' COMMENT '角色ID',
  `pid` smallint(2) NOT NULL DEFAULT '0' COMMENT '权限ID',
  KEY `role_permission_rid` (`rid`) USING BTREE,
  KEY `role_permission_pid` (`pid`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Table structure for mt_url_access_strategy
-- ----------------------------
DROP TABLE IF EXISTS `mt_url_access_strategy`;
CREATE TABLE `mt_url_access_strategy` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '自增id',
  `url` varchar(100) NOT NULL DEFAULT '' COMMENT '访问地址',
  `filters` varchar(100) NOT NULL DEFAULT '' COMMENT '访问地址过滤器及权限',
  `priorities` smallint(2) NOT NULL DEFAULT '1' COMMENT '地址过滤器优先顺序（数值越小优先级越高）',
  `status` tinyint(1) NOT NULL DEFAULT '1' COMMENT '配置状态，1==》正常，2==》禁用',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Table structure for mt_user_account
-- ----------------------------
DROP TABLE IF EXISTS `mt_user_account`;
CREATE TABLE `mt_user_account` (
  `mobile` bigint(8) NOT NULL DEFAULT '0' COMMENT '用户手机号（用户id）',
  `password` varchar(100) NOT NULL DEFAULT '' COMMENT '用户密码',
  `type` int(1) NOT NULL DEFAULT '1' COMMENT '用户类型：1==》普通，2==》捐赠，3==》内部',
  `status` int(1) NOT NULL DEFAULT '1' COMMENT '用户状态：1==》正常，2==》锁定，3==》禁用',
  `reg_date` bigint(8) NOT NULL DEFAULT '0' COMMENT '用户注册日期',
  PRIMARY KEY (`mobile`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Table structure for mt_user_info
-- ----------------------------
DROP TABLE IF EXISTS `mt_user_info`;
CREATE TABLE `mt_user_info` (
  `mobile` bigint(8) NOT NULL DEFAULT '0' COMMENT '用户id',
  `nickname` varchar(50) NOT NULL DEFAULT '' COMMENT '用户昵称',
  `avatar_id` int(2) NOT NULL DEFAULT '0' COMMENT '用户头像id',
  `gender` int(1) NOT NULL DEFAULT '1' COMMENT '用户性别：1==》Male，2==》Female',
  `honor_id` int(1) NOT NULL DEFAULT '0' COMMENT '用户头像id',
  `total_donation` int(4) DEFAULT '0' COMMENT '用户总捐赠金额',
  `surplus_sms` int(2) DEFAULT '0' COMMENT '用户剩余短信数',
  `last_login_time` bigint(8) DEFAULT '0' COMMENT '用户最后一次登录时间',
  `update_time` bigint(8) DEFAULT '0' COMMENT '用户信息更新日期',
  PRIMARY KEY (`mobile`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Table structure for mt_user_role
-- ----------------------------
DROP TABLE IF EXISTS `mt_user_role`;
CREATE TABLE `mt_user_role` (
  `mobile` bigint(8) NOT NULL DEFAULT '0' COMMENT '用户ID',
  `rid` smallint(2) NOT NULL DEFAULT '0' COMMENT '角色ID',
  PRIMARY KEY (`mobile`,`rid`),
  KEY `user_role_rid` (`rid`) USING BTREE,
  KEY `user_role_uid` (`mobile`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
