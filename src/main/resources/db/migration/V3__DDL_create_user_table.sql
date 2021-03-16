CREATE TABLE `t_user` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '自增id',
  `uid` varchar(64) NOT NULL COMMENT '自定义用户唯一标识',
  `name` varchar(64) NOT NULL COMMENT '用户名称',
  `portrait` varchar(255) NOT NULL COMMENT '用户头像',
  `mobile` varchar(11) DEFAULT NULL COMMENT '手机号',
  `type` tinyint(4) NOT NULL COMMENT '用户类型，0:注册用户 1:游客',
  `device_id` varchar(64) NOT NULL COMMENT '设备ID',
  `create_dt` datetime NOT NULL,
  `update_dt` datetime NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uid_unique` (`uid`),
  KEY `deviceId_index` (`device_id`) USING BTREE,
  KEY `mobile_index` (`mobile`) USING BTREE
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci;