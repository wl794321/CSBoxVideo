package wiki.csbox.content.service.impl;

import wiki.csbox.content.model.po.Teachplan;
import wiki.csbox.content.mapper.TeachplanMapper;
import wiki.csbox.content.service.TeachplanService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * <p>
 * 课程计划 服务实现类
 * </p>
 *
 * @author Krian
 */
@Slf4j
@Service
public class TeachplanServiceImpl extends ServiceImpl<TeachplanMapper, Teachplan> implements TeachplanService {

}
