package com.mmz.web;

import com.mmz.dto.Exposer;
import com.mmz.dto.SeckillExecution;
import com.mmz.dto.SeckillResult;
import com.mmz.entity.Seckill;
import com.mmz.enums.SeckillStatEnum;
import com.mmz.exception.RepeatKillException;
import com.mmz.exception.SeckillCloseException;
import com.mmz.service.SeckillService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

/**
 * @author : mengmuzi
 * create at:  2019-07-22  17:31
 * @description: WEB层
 */
@Controller
@RequestMapping("/seckill")//url:模块/资源/{}/细分
public class SeckillController {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private SeckillService seckillService;

    /**
     * 秒杀API的URL设计
     * 1. 秒杀列表
     *  GET/seckill/list
     */
    @RequestMapping(value= "/list",method = RequestMethod.GET)
    public String list(Model model){
        //list.jsp + model = ModelAndView
        //获取列表页
        List<Seckill> list = seckillService.getSeckillList();
        model.addAttribute("list",list);
        return "list"; //WEB-INF/jsp/list.jsp
    }

    /**
     * 秒杀API的URL设计
     * 2. 详情页
     *  GET/seckill/{id}/detail
     */
    @RequestMapping(value = "/{seckillId}/detail", method = RequestMethod.GET)
    public String detail(@PathVariable("seckillId") Long seckillId , Model model){
        if(seckillId == null){
            return "redirect:/seckill/list";//重定向
        }

        Seckill seckill = seckillService.getById(seckillId);
        if(seckill == null){
            return "forward:/seckill/list";
        }
         model.addAttribute("seckill",seckill);
        return "detail";
    }

    //ajax接口 ,json暴露秒杀接口的方法
    /**
     * 秒杀API的URL设计
     * 3.暴露秒杀
     *  POST/seckill/{id}/exposer
     */
    @RequestMapping(value = "/{seckillId}/exposer",
                    method = RequestMethod.POST,
                    produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public SeckillResult<Exposer> exposer(@PathVariable("seckillId") Long seckillId){
        SeckillResult<Exposer> result;
        try {
            Exposer exposer = seckillService.exportSeckillUrl(seckillId);
            result = new SeckillResult<Exposer>(true,exposer);
        }catch (Exception e) {
            logger.error(e.getMessage(), e);
            result = new SeckillResult<Exposer>(false, e.getMessage());
        }
        return result;
    }

    /**
     * 秒杀API的URL设计
     * 4.执行秒杀
     *  POST/seckill/{id}/{md5}/execution
     */
    @RequestMapping(value = "/{seckillId}/{md5}/execution",
                    method = RequestMethod.POST,
                    produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    /**
     * 注意这里的手机号是从浏览器的Cookie中取得的
     * SpringMVC在处理Cookie时有个小问题：如果找不到对应的Cookie会报错，
     * 所以设置为required=false，将Cookie是否存在的逻辑判断放到代码中来判断。
     */
    public SeckillResult<SeckillExecution> execute(@PathVariable("seckillId") Long seckillId,
                                                   @PathVariable("md5") String md5,
                                                   @CookieValue(value = "userPhone",required = false) Long userPhone){
        if(userPhone == null){
            return new SeckillResult<SeckillExecution>(false,"很遗憾，您没有注册");
        }
        try{
            SeckillExecution execution = seckillService.executeSeckill(seckillId,userPhone,md5);
            return new SeckillResult<SeckillExecution>(true,execution);
        } catch (RepeatKillException e1){
            SeckillExecution execution = new SeckillExecution(seckillId, SeckillStatEnum.REPEAT_KILL);
            return new SeckillResult<SeckillExecution>(true,execution);
        } catch (SeckillCloseException e2){
            SeckillExecution execution = new SeckillExecution(seckillId,SeckillStatEnum.END);
            return new SeckillResult<SeckillExecution>(true,execution);
        } catch (Exception e){
            logger.error(e.getMessage(),e);
            SeckillExecution execution = new SeckillExecution(seckillId,SeckillStatEnum.INNER_ERROR);
            return new SeckillResult<SeckillExecution>(true,execution);
        }

    }

    /**
     * 秒杀API的URL设计
     * 5.获取系统时间
     *  GET/seckill/time/now
     */
    @RequestMapping(value = "/time/now", method = RequestMethod.GET)
    @ResponseBody
    public SeckillResult<Long> time(){
        Date now = new Date();
        return new SeckillResult<Long>(true,now.getTime());
    }

}
