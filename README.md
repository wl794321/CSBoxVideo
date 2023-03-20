# <center>🎉 在线视频项目 🎉</center>

<hr/>

## 工程模块：

### 技术选选型：

前端：

- TypeScript
- Vue
- Element UI


后端：
- Spring、Spring MVC、Spring Boot
- MyBatis、MyBatisPlus
- MySQL

### 工程模块：


<hr/>


## 数据库模块：

### 视频信息模块：

#### csbox-video-content

- course-base：课程视频基础信息表

```SQL
-- 课程基本信息 
-- auto-generated definition
create table course_base
(
    id            bigint auto_increment comment '主键'
        primary key,
    company_id    bigint                  not null comment '机构ID',
    company_name  varchar(255)            null comment '机构名称',
    name          varchar(100)            not null comment '课程名称',
    users         varchar(500)            null comment '适用人群',
    tags          varchar(50)             null comment '课程标签',
    mt            varchar(20)             not null comment '大分类',
    st            varchar(20)             not null comment '小分类',
    grade         varchar(32)             not null comment '课程等级',
    teachmode     varchar(32)             not null comment '教育模式(common普通，record 录播，live直播等）',
    description   text                    null comment '课程介绍',
    pic           varchar(500)            null comment '课程图片',
    create_date   datetime                null comment '创建时间',
    change_date   datetime                null comment '修改时间',
    create_people varchar(50)             null comment '创建人',
    change_people varchar(50)             null comment '更新人',
    audit_status  varchar(10)             not null comment '审核状态',
    status        varchar(10) default '1' not null comment '课程发布状态 未发布  已发布 下线'
)
    comment '课程基本信息' charset = utf8mb3;
```

- course-category：课程视频分类信息表

```sql
-- 课程分类 
-- auto-generated definition
create table course_category
(
    id       varchar(20)             not null comment '主键'
        primary key,
    name     varchar(32)             not null comment '分类名称',
    label    varchar(32)             null comment '分类标签默认和名称一样',
    parentid varchar(20) default '0' not null comment '父结点id（第一级的父节点是0，自关联字段id）',
    is_show  tinyint                 null comment '是否显示',
    orderby  int                     null comment '排序字段',
    is_leaf  tinyint                 null comment '是否叶子'
)
    comment '课程分类' charset = utf8mb3;
```

使用 `parentid` 来维护树形结构，指向父级Id（同理：评论树的实现）。当树型结构比较固定时，可以使用表的自连接进行树形结构的查询；当树的结构不固定时
可以使用MySQL8的新特性递归with语法，查询树形结构。

```sql
--- 递归查询
with recursive treeNode as (select *
                            from course_category
                            where id = '1'
                            union all
                            select category.*
                            from course_category as category
                                     inner join treeNode
                                                on treeNode.id = category.parentid)
select *
from treeNode
order by treeNode.id;
```
- course_market：课程营销信息表

```sql
-- 课程营销信息 
-- auto-generated definition
create table course_market
(
    id             bigint       not null comment '主键，课程id'
        primary key,
    charge         varchar(32)  not null comment '收费规则，对应数据字典',
    price          float(10, 2) null comment '现价',
    original_price float(10, 2) null comment '原价',
    qq             varchar(32)  null comment '咨询qq',
    wechat         varchar(64)  null comment '微信',
    phone          varchar(32)  null comment '电话',
    valid_days     int          null comment '有效期天数'
)
    comment '课程营销信息' charset = utf8mb3;
```

