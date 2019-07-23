package com.mmz.service;

import com.mmz.dto.Exposer;
import com.mmz.dto.SeckillExecution;
import com.mmz.entity.Seckill;
import com.mmz.exception.RepeatKillException;
import com.mmz.exception.SeckillCloseException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:spring/spring-dao.xml","classpath:spring/spring-service.xml"})
public class SeckillServiceTest {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private SeckillService seckillService;

    @Test
    public void testGetSeckillList() {
        List<Seckill> list = seckillService.getSeckillList();
        logger.info("list={}",list);
    }

    @Test
    public void testGetById() {
        long seckillId = 1000L;
        Seckill seckill = seckillService.getById(seckillId);
        logger.info("seckill={}",seckill);
    }

    @Test
    public void testExportSeckillUrl() {
        long seckillId = 1001L;
        Exposer exposer = seckillService.exportSeckillUrl(seckillId);
        logger.info("exposer={}",exposer);
        /**
         * [main] INFO  com.mmz.service.SeckillServiceTest - exposer=Exposer{exposed=true,
         * md5='4df64683e6233fd217c774a3a369964a', seckillId=1000, now=0, start=0, end=0}
         */
    }

    @Test
    public void testExecuteSeckill() {
        long seckillId = 1000L;
        long userPhone = 13814393597L;
        String md5 = "4df64683e6233fd217c774a3a369964a";
        try {
            SeckillExecution execution = seckillService.executeSeckill(seckillId, userPhone, md5);
            logger.info("result={}", execution);
        }catch (RepeatKillException e)
        {
            logger.error(e.getMessage());
        }catch (SeckillCloseException e1)
        {
            logger.error(e1.getMessage());
        }
    }


    /**
     * 在测试过程中，第四个方法使用到了第三个方法返回的秒杀地址，
     * 在实际开发中，我们需要将第三个和第四个方法合并成一个完整逻辑的方法
     *
     */
    @Test
    public void testSeckillLogic() throws Exception {
        long seckillId = 1000;
        Exposer exposer = seckillService.exportSeckillUrl(seckillId);
        if (exposer.isExposed()) {
            logger.info("exposer={}", exposer);
            long userPhone = 13814393596L;
            String md5 = exposer.getMd5();

            try {
                SeckillExecution execution = seckillService.executeSeckill(seckillId, userPhone, md5);
                logger.info("result={}", execution);
            }catch (RepeatKillException e) {
                logger.error(e.getMessage());
            }catch (SeckillCloseException e1) {
                logger.error(e1.getMessage());
            }
        }else {
            //秒杀未开启
            logger.warn("exposer={}", exposer);
        }
    }

}