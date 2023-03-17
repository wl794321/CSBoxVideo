package wiki.csbox.content.service.impl;

import wiki.csbox.content.model.po.TeachplanMedia;
import wiki.csbox.content.mapper.TeachplanMediaMapper;
import wiki.csbox.content.service.TeachplanMediaService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author Krian
 */
@Slf4j
@Service
public class TeachplanMediaServiceImpl extends ServiceImpl<TeachplanMediaMapper, TeachplanMedia> implements TeachplanMediaService {

}
