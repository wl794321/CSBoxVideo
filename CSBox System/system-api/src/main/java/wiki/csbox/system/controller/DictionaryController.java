package wiki.csbox.system.controller;

import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.*;
import wiki.csbox.system.model.po.Dictionary;
import wiki.csbox.system.service.DictionaryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * <p>
 * 数据字典 前端控制器
 * </p>
 *
 * @author itcast
 */
@Api(value = "数据字典接口", tags = "数据字典")
@CrossOrigin
@Slf4j
@RestController
@RequestMapping("/system")
public class DictionaryController  {

    @Autowired
    private DictionaryService  dictionaryService;

    @GetMapping("/dictionary/all")
    public List<Dictionary> queryAll() {
        return dictionaryService.queryAll();
    }

    @GetMapping("/dictionary/code/{code}")
    public Dictionary getByCode(@PathVariable String code) {
        return dictionaryService.getByCode(code);
    }
}
