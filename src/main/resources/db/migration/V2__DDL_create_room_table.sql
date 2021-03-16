CREATE TABLE `t_room` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '自增id',
  `uid` varchar(64) NOT NULL COMMENT '自定义唯一标识',
  `name` varchar(64) NOT NULL COMMENT '房间名称',
  `theme_picture_url` varchar(1024) NOT NULL COMMENT '房间主题图片',
  `allowed_join_room` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否允许观众加入房间 0否1是',
  `allowed_free_join_mic` tinyint(1) NOT NULL,
  `create_dt` datetime DEFAULT NULL,
  `update_dt` datetime DEFAULT NULL,
  `type` tinyint(4) NOT NULL DEFAULT '1' COMMENT '房间类型：0 官方房间，1自建房间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uid_unique` (`uid`)
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci;