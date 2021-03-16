CREATE TABLE `t_appversion` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '自增id',
  `platform` varchar(16) NOT NULL COMMENT '1:Android, 2:iOS',
  `download_url` varchar(255) NOT NULL COMMENT '下载地址',
  `version` varchar(36) NOT NULL COMMENT '版本号',
  `version_code` bigint(32) NOT NULL,
  `force_upgrade` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否强制更新',
  `release_note` text COMMENT '版本描述',
  `create_dt` datetime NOT NULL,
  `update_dt` datetime NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci;