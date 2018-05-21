package cn.appsys.controller.developer;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.alibaba.fastjson.JSONArray;
import com.mysql.jdbc.StringUtils;

import cn.appsys.pojo.AppCategory;
import cn.appsys.pojo.AppInfo;
import cn.appsys.pojo.AppVersion;
import cn.appsys.pojo.DataDictionary;
import cn.appsys.pojo.DevUser;
import cn.appsys.service.developer.AppCategoryService;
import cn.appsys.service.developer.AppInfoService;
import cn.appsys.service.developer.AppVersionService;
import cn.appsys.service.developer.DataDictionaryService;
import cn.appsys.tools.Constants;
import cn.appsys.tools.PageSupport;

@Controller
@RequestMapping("appinfo")
public class AppController {

	private Logger logger = Logger.getLogger(AppController.class);
	@Resource
	private AppInfoService appInfoService;
	@Resource
	private DataDictionaryService dataDictionaryService;
	@Resource
	private AppCategoryService appCategoryService;
	@Resource
	private AppVersionService appVersionService;

	HashMap<String, String> resultMap = new HashMap<String, String>();

	// 查询列表
	@RequestMapping(value = "/list")
	public String getAppInfoList(Model model, HttpSession session,
			@RequestParam(value = "querySoftwareName", required = false) String querySoftwareName,
			@RequestParam(value = "queryStatus", required = false) String _queryStatus,
			@RequestParam(value = "queryCategoryLevel1", required = false) String _queryCategoryLevel1,
			@RequestParam(value = "queryCategoryLevel2", required = false) String _queryCategoryLevel2,
			@RequestParam(value = "queryCategoryLevel3", required = false) String _queryCategoryLevel3,
			@RequestParam(value = "queryFlatformId", required = false) String _queryFlatformId,
			@RequestParam(value = "pageIndex", required = false) String pageIndex) {

		Integer devId = ((DevUser) session.getAttribute(Constants.DEV_USER_SESSION)).getId();
		List<AppInfo> appInfoList = null;
		List<DataDictionary> statusList = null;
		List<DataDictionary> flatFormList = null;
		List<AppCategory> categoryLevel1List = null;// 列出一级分类列表，注：二级和三级分类列表通过异步ajax获取
		List<AppCategory> categoryLevel2List = null;
		List<AppCategory> categoryLevel3List = null;
		// 页面容量
		int pageSize = Constants.pageSize;
		// 当前页码
		Integer currentPageNo = 1;

		if (pageIndex != null) {
			try {
				currentPageNo = Integer.valueOf(pageIndex);
			} catch (NumberFormatException e) {
				// TODO: handle exception
				e.printStackTrace();
			}
		}
		Integer queryStatus = null;
		if (_queryStatus != null && !_queryStatus.equals("")) {
			queryStatus = Integer.parseInt(_queryStatus);
		}
		Integer queryCategoryLevel1 = null;
		if (_queryCategoryLevel1 != null && !_queryCategoryLevel1.equals("")) {
			queryCategoryLevel1 = Integer.parseInt(_queryCategoryLevel1);
		}
		Integer queryCategoryLevel2 = null;
		if (_queryCategoryLevel2 != null && !_queryCategoryLevel2.equals("")) {
			queryCategoryLevel2 = Integer.parseInt(_queryCategoryLevel2);
		}
		Integer queryCategoryLevel3 = null;
		if (_queryCategoryLevel3 != null && !_queryCategoryLevel3.equals("")) {
			queryCategoryLevel3 = Integer.parseInt(_queryCategoryLevel3);
		}
		Integer queryFlatformId = null;
		if (_queryFlatformId != null && !_queryFlatformId.equals("")) {
			queryFlatformId = Integer.parseInt(_queryFlatformId);
		}

		// 总数量（表）
		int totalCount = 0;
		try {
			totalCount = appInfoService.getAppInfoCount(querySoftwareName, queryStatus, queryCategoryLevel1,
					queryCategoryLevel2, queryCategoryLevel3, queryFlatformId, devId);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// 总页数
		PageSupport pages = new PageSupport();
		pages.setCurrentPageNo(currentPageNo);
		pages.setPageSize(pageSize);
		pages.setTotalCount(totalCount);
		int totalPageCount = pages.getTotalPageCount();
		// 控制首页和尾页
		if (currentPageNo < 1) {
			currentPageNo = 1;
		} else if (currentPageNo > totalPageCount) {
			currentPageNo = totalPageCount;
		}
		try {
			appInfoList = appInfoService.getAppInfoList(querySoftwareName, queryStatus, queryCategoryLevel1,
					queryCategoryLevel2, queryCategoryLevel3, queryFlatformId, devId, currentPageNo, pageSize);
			statusList = this.getDataDictionaryList("APP_STATUS");
			flatFormList = this.getDataDictionaryList("APP_FLATFORM");
			categoryLevel1List = appCategoryService.getAppCategoryListByParentId(null);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		model.addAttribute("appInfoList", appInfoList);
		model.addAttribute("statusList", statusList);
		model.addAttribute("flatFormList", flatFormList);
		model.addAttribute("categoryLevel1List", categoryLevel1List);
		model.addAttribute("pages", pages);
		model.addAttribute("queryStatus", queryStatus);
		model.addAttribute("querySoftwareName", querySoftwareName);
		model.addAttribute("queryCategoryLevel1", queryCategoryLevel1);
		model.addAttribute("queryCategoryLevel2", queryCategoryLevel2);
		model.addAttribute("queryCategoryLevel3", queryCategoryLevel3);
		model.addAttribute("queryFlatformId", queryFlatformId);

		// 二级分类列表和三级分类列表---回显
		if (queryCategoryLevel2 != null && !queryCategoryLevel2.equals("")) {
			categoryLevel2List = getCategoryList(queryCategoryLevel1.toString());
			model.addAttribute("categoryLevel2List", categoryLevel2List);
		}
		if (queryCategoryLevel3 != null && !queryCategoryLevel3.equals("")) {
			categoryLevel3List = getCategoryList(queryCategoryLevel2.toString());
			model.addAttribute("categoryLevel3List", categoryLevel3List);
		}
		return "developer/appinfolist";
	}

	public List<DataDictionary> getDataDictionaryList(String typeCode) {
		List<DataDictionary> dataDictionaryList = null;
		try {
			dataDictionaryList = dataDictionaryService.getDataDictionaryList(typeCode);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return dataDictionaryList;
	}

	/**
	 * 根据typeCode查询出相应的数据字典列表
	 * 
	 * @param pid
	 * @return
	 */
	@RequestMapping(value = "/datadictionarylist.json", method = RequestMethod.GET)
	@ResponseBody
	public List<DataDictionary> getDataDicList(@RequestParam String tcode) {
		logger.debug("getDataDicList tcode ============ " + tcode);
		return this.getDataDictionaryList(tcode);
	}

	/**
	 * 根据parentId查询出相应的分类级别列表
	 * 
	 * @param pid
	 * @return
	 */
	@RequestMapping(value = "/categorylevellist.json", method = RequestMethod.GET)
	@ResponseBody
	public List<AppCategory> getAppCategoryList(@RequestParam String pid) {
		logger.debug("getAppCategoryList pid ============ " + pid);
		if (pid.equals(""))
			pid = null;
		return getCategoryList(pid);
	}

	public List<AppCategory> getCategoryList(String pid) {
		List<AppCategory> categoryLevelList = null;
		try {
			categoryLevelList = appCategoryService
					.getAppCategoryListByParentId(pid == null ? null : Integer.parseInt(pid));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return categoryLevelList;
	}

	// 删除
	@RequestMapping("delete")
	@ResponseBody
	public Object delete(@RequestParam String id) {
		try {
			if (StringUtils.isNullOrEmpty(id)) {
				resultMap.put("delResult", "notexist");
			} else {
				boolean result = appInfoService.appsysdeleteAppById(Integer.parseInt(id));
				if (result) {
					resultMap.put("delResult", "true");
				} else {
					resultMap.put("delResult", "false");
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return JSONArray.toJSONString(resultMap);
	}

	// 新增app基础信息
	@RequestMapping("add")
	public String add(AppInfo appinfo) {
		try {
			boolean result = appInfoService.add(appinfo);
			if (result) {
				resultMap.put("addResult", "true");
			} else {
				resultMap.put("addResult", "false");
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "redirect:/appinfo/list";
	}

	// 单击新增按钮跳转到新增页面
	@RequestMapping("add2")
	public String add2() {
		return "developer/appinfoadd";
	}

	// 新增信息时上传logo图片
	@RequestMapping(value = "/appinfoaddsave", method = RequestMethod.POST)
	public String addSave(AppInfo appInfo, HttpSession session, HttpServletRequest request,
			@RequestParam(value = "a_logoPicPath", required = false) MultipartFile attach) {
		String logoPicPath = null;
		String logoLocPath = null;
		if (!attach.isEmpty()) {
			String path = request.getSession().getServletContext()
					.getRealPath("statics" + java.io.File.separator + "uploadfiles");
			logger.info("uploadFile path: " + path);
			String oldFileName = attach.getOriginalFilename();// 原文件名
			String prefix = FilenameUtils.getExtension(oldFileName);// 原文件后缀
			int filesize = 500000;
			if (attach.getSize() > filesize) {// 上传大小不得超过 50k
				request.setAttribute("fileUploadError", Constants.FILEUPLOAD_ERROR_4);
				return "developer/appinfoadd";
			} else if (prefix.equalsIgnoreCase("jpg") || prefix.equalsIgnoreCase("png")
					|| prefix.equalsIgnoreCase("jepg") || prefix.equalsIgnoreCase("pneg")) {// 上传图片格式
				String fileName = appInfo.getAPKName() + ".jpg";// 上传LOGO图片命名:apk名称.apk
				File targetFile = new File(path, fileName);
				if (!targetFile.exists()) {
					targetFile.mkdirs();
				}
				try {
					attach.transferTo(targetFile);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					request.setAttribute("fileUploadError", Constants.FILEUPLOAD_ERROR_2);
					return "developer/appinfoadd";
				}
				logoPicPath = request.getContextPath() + "/statics/uploadfiles/" + fileName;
				logoLocPath = path + File.separator + fileName;
			} else {
				request.setAttribute("fileUploadError", Constants.FILEUPLOAD_ERROR_3);
				return "developer/appinfoadd";
			}
		}
		appInfo.setCreatedBy(((DevUser) session.getAttribute(Constants.DEV_USER_SESSION)).getId());
		appInfo.setCreationDate(new Date());
		appInfo.setLogoPicPath(logoPicPath);
		appInfo.setLogoLocPath(logoLocPath);
		appInfo.setDevId(((DevUser) session.getAttribute(Constants.DEV_USER_SESSION)).getId());
		appInfo.setStatus(1);
		try {
			if (appInfoService.add(appInfo)) {
				return "redirect:/dev/flatform/app/list";
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "developer/appinfoadd";
	}

	// 异步校验apkName是否存在
	@RequestMapping(value = "apkexist.json", method = RequestMethod.GET)
	@ResponseBody
	public Object apkNameIsExist(@RequestParam String APKName) {
		if (StringUtils.isNullOrEmpty(APKName)) {
			resultMap.put("APKName", "empty");
		} else {
			AppInfo appinfo = null;
			try {
				appinfo = appInfoService.getAppInfo(null, APKName);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (null != appinfo) {
				resultMap.put("APKName", "exist");
			} else {
				resultMap.put("APKName", "noexist");
			}
		}
		return JSONArray.toJSONString(resultMap);
	}

	// 修改基础信息前先去查询数据
	@RequestMapping("appinfomodify")
	public String appinfomodify(@RequestParam Integer id, Model model) {
		try {
			AppInfo appInfo = appInfoService.getAppInfo(id, null);
			model.addAttribute(appInfo);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "developer/appinfomodify";
	}

	// 修改app基础信息
	@RequestMapping("modify")
	public String modify(AppInfo appinfo) {
		try {
			boolean result = appInfoService.modify(appinfo);
			if (result) {
				resultMap.put("modifyResult", "true");
			} else {
				resultMap.put("modifyResult", "false");
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "redirect:/appinfo/list";
	}

	// 单击新增版本按钮跳转到页面
	@RequestMapping(value = "appversionadd")
	public String appversionadd(@RequestParam(value = "id") String id, AppVersion appVersion, Model model) {
		appVersion.setAppId(Integer.parseInt(id));
		model.addAttribute(appVersion);
		return "developer/appversionadd";
	}

	// 新增版本信息
	@RequestMapping("addversionsave")
	public String addversionsave(AppVersion appVersion) throws Exception {
		boolean flag = appVersionService.appsysadd(appVersion);
		if (flag) {
			return "redirect:/appinfo/list";
		} else {
			return "redirect:/appinfo/appversionadd";
		}
	}

	// 单击修改版本按钮跳转到页面
	@RequestMapping(value = "appversionmodify")
	public String appversionmodify(@RequestParam(value = "vid") String id, AppVersion appVersion, Model model)
			throws NumberFormatException, Exception {
		appVersion = appVersionService.getAppVersionById(Integer.parseInt(id));
		model.addAttribute(appVersion);
		return "developer/appversionmodify";
	}

	// 修改版本信息
	@RequestMapping("appversionmodifysave")
	public String appversionmodifysave(AppVersion appVersion) throws Exception {
		boolean flag = appVersionService.modify(appVersion);
		if (flag) {
			return "redirect:/appinfo/list";
		} else {
			return "redirect:/appinfo/appversionmodify";
		}
	}

	// 查看
	@RequestMapping(value = "appview")
	public String appview(@RequestParam(value = "id") String id, Model model) throws NumberFormatException, Exception {
		List<AppVersion> appVersionList = new ArrayList<AppVersion>();
		appVersionList = appVersionService.getAppVersionList(Integer.parseInt(id));
		AppInfo appinfo = appInfoService.getAppInfo(Integer.parseInt(id), null);
		model.addAttribute(appVersionList);
		model.addAttribute(appinfo);
		return "developer/appinfoview";
	}

	// 上、下架
	@RequestMapping(value = "/{appid}/sale", method = RequestMethod.PUT)
	@ResponseBody
	public Object sale(@PathVariable String appid, HttpSession session) {
		HashMap<String, Object> resultMap = new HashMap<String, Object>();
		Integer appIdInteger = 0;
		try {
			appIdInteger = Integer.parseInt(appid);
		} catch (Exception e) {
			appIdInteger = 0;
		}
		resultMap.put("errorCode", "0");
		resultMap.put("appId", appid);
		if (appIdInteger > 0) {
			try {
				DevUser devUser = (DevUser) session.getAttribute(Constants.DEV_USER_SESSION);
				AppInfo appInfo = new AppInfo();
				appInfo.setId(appIdInteger);
				appInfo.setModifyBy(devUser.getId());
				if (appInfoService.appsysUpdateSaleStatusByAppId(appInfo)) {
					resultMap.put("resultMsg", "success");
				} else {
					resultMap.put("resultMsg", "success");
				}
			} catch (Exception e) {
				resultMap.put("errorCode", "exception000001");
			}
		} else {
			// errorCode:0为正常
			resultMap.put("errorCode", "param000001");
		}

		/*
		 * resultMsg:success/failed errorCode:exception000001 appId:appId
		 * errorCode:param000001
		 */
		return resultMap;
	}
}
