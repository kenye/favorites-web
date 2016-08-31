package com.favorites.web;

import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.favorites.domain.CollectRepository;
import com.favorites.domain.Favorites;
import com.favorites.domain.FavoritesRepository;
import com.favorites.domain.result.ExceptionMsg;
import com.favorites.domain.result.Response;
import com.favorites.domain.result.ResponseData;
import com.favorites.service.FavoritesService;
import com.favorites.utils.DateUtils;

@RestController
@RequestMapping("/favorites")
public class FavoritesController extends BaseController{
	
	@Autowired
	private FavoritesRepository favoritesRepository;
	@Resource
	private FavoritesService favoritesService;
	@Autowired
	private CollectRepository collectRepository;
	
	/**
	 * 添加
	 * @param name
	 * @return
	 */
	@RequestMapping(value="/add",method=RequestMethod.POST)
	public Response addFavorites(String name){
		if(StringUtils.isNotBlank(name)){
			Favorites favorites = favoritesRepository.findByUserIdAndName(getUserId(), name);
			if(null != favorites){
				logger.info("收藏夹名称已被创建");
				return result(ExceptionMsg.FavoritesNameUsed);
			}else{
				try {
					favoritesService.saveFavorites(getUserId(), 0l, name);
				} catch (Exception e) {
					logger.error("异常：",e);
					return result(ExceptionMsg.FAILED);
				}
			}
		}else{
			logger.info("收藏夹名称为空");
			return result(ExceptionMsg.FavoritesNameIsNull);
		}
		return result();
	}
	
	/**
	 * 创建导入收藏夹
	 * @return
	 */
	@RequestMapping(value="/addImportFavorites",method=RequestMethod.POST)
	public ResponseData addImportFavorites(){
		Favorites favorites = favoritesRepository.findByUserIdAndName(getUserId(), "导入自浏览器");
		if(null == favorites){
			try {
				favorites = favoritesService.saveFavorites(getUserId(), 0l, "导入自浏览器");
			} catch (Exception e) {
				logger.error("异常：",e);				
			}
		}
		return new ResponseData(ExceptionMsg.SUCCESS, favorites.getId());
	}
	
	/**
	 * 修改
	 * @param favoritesName
	 * @param favoritesId
	 * @return
	 */
	@RequestMapping(value="/update",method=RequestMethod.POST)
	public Response updateFavorites(String favoritesName,Long favoritesId){
		logger.info("param favoritesName:" + favoritesName + "----favoritesId:" + favoritesId);
		if(StringUtils.isNotBlank(favoritesName)&& null != favoritesId){
			Favorites favorites = favoritesRepository.findByUserIdAndName(getUserId(), favoritesName);
			if(null != favorites){
				logger.info("收藏夹名称已被创建");
				return result(ExceptionMsg.FavoritesNameUsed);
			}else{
				try {
					favoritesRepository.updateNameById(favoritesId, DateUtils.getCurrentTime(), favoritesName);
				} catch (Exception e) {
					logger.error("修改收藏夹名称异常：",e);
				}
			}
		}else{
			logger.info("参数错误name:" + favoritesName +"----" + "id:" + favoritesId);
			return result(ExceptionMsg.FAILED);
		}
		return result();
	}
	
	/**
	 * 删除
	 * @param id
	 * @return
	 */
	@RequestMapping(value="/del",method=RequestMethod.POST)
	public Response delFavorites(Long id){
		logger.info("param id:" + id);
		if(null == id){
			return result(ExceptionMsg.FAILED);
		}
		try {
			favoritesRepository.delete(id);
			// 删除该收藏夹下文章
			collectRepository.deleteByFavoritesId(id);
		} catch (Exception e) {
			logger.error("删除异常：",e);
		}
		return result();
	}
	
	/**
	 * 获取收藏夹
	 * @return
	 */
	@RequestMapping(value = "/getFavorites/{userId}", method = RequestMethod.POST)
	public List<Favorites> getFavorites(@PathVariable("userId") Long userId) {
		logger.info("getFavorites begin");
		List<Favorites> favorites = null;
		try {
			Long id = getUserId();
			if(null != userId && 0 != userId){
				id = userId;
			}
			favorites = favoritesRepository.findByUserIdOrderByIdDesc(id);
		} catch (Exception e) {
			// TODO: handle exception
			logger.error("getFavorites failed, ", e);
		}
		logger.info("getFavorites end favorites ==" );
		return favorites;
	}

}
