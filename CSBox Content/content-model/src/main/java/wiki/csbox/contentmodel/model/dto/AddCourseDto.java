package wiki.csbox.contentmodel.model.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import wiki.csbox.csboxbase.constant.ValidationGroups;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

/**
 * @author Krian
 * @version 1.0
 * @description 添加课程dto
 * @date 2023/3/18 0018 10:58
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@ApiModel(value = "AddCourseDto", description = "新增课程基本信息")
public class AddCourseDto {

    // 分组校验：
    // @NotEmpty(message = "新增课程名称不能为空", groups = {ValidationGroups.Insert.class})
    // @NotEmpty(message = "更新课程名称不能为空", groups = {ValidationGroups.Update.class})
    @ApiModelProperty(value = "课程名称", required = true)
    private String name;

    @NotEmpty(message = "适用人群不能为空")
    @Size(message = "适用人群人数过少", min = 10)
    @ApiModelProperty(value = "适用人群", required = true)
    private String users;

    @ApiModelProperty(value = "课程标签")
    private String tags;

    @NotEmpty(message = "课程分类不能为空")
    @ApiModelProperty(value = "大分类", required = true)
    private String mt;

    @NotEmpty(message = "课程分类不能为空")
    @ApiModelProperty(value = "小分类", required = true)
    private String st;

    @NotEmpty(message = "课程等级不能为空")
    @ApiModelProperty(value = "课程等级", required = true)
    private String grade;

    @ApiModelProperty(value = "教学模式（普通，录播，直播等）", required = true)
    private String teachmode;

    @ApiModelProperty(value = "课程介绍")
    private String description;

    @ApiModelProperty(value = "课程图片", required = true)
    private String pic;

    @NotEmpty(message = "收费规则不能为空")
    @ApiModelProperty(value = "收费规则，对应数据字典", required = true)
    private String charge;

    @ApiModelProperty(value = "价格")
    private Float price;
    @ApiModelProperty(value = "原价")
    private Float originalPrice;

    @ApiModelProperty(value = "qq")
    private String qq;

    @ApiModelProperty(value = "微信")
    private String wechat;
    @ApiModelProperty(value = "电话")
    private String phone;

    @ApiModelProperty(value = "有效期")
    private Integer validDays;
}
