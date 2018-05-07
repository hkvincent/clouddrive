package com.vincent.Catalog.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.vincent.Catalog.dao.CatalogDao;
import com.vincent.Catalog.domain.Catalog;
import com.vincent.Catalog.service.CatalogService;
import com.vincent.Util.Tool;
import com.vincent.user.domain.User;

import cn.itcast.servlet.BaseServlet;

@SuppressWarnings("serial")
public class CatalogServlet extends BaseServlet {
	private CatalogService service = new CatalogService();
	ThreadLocal<CatalogService> threadList = new ThreadLocal<CatalogService>();

	@SuppressWarnings("unchecked")
	public String myCatalog(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		service = threadList.get() == null ? new CatalogService() : threadList
				.get();
		
		//面包屑的指引栏
		LinkedHashSet<Catalog> path = (LinkedHashSet<Catalog>) request
				.getSession().getAttribute("path");
		if (path == null) {
			path = new LinkedHashSet<Catalog>();
			request.getSession().setAttribute("path", path);
		}
		//直接进入Home page的时候进行验证
		String cid = request.getParameter("cid");
		if (cid == null || cid.isEmpty()) {
			User user = (User) request.getSession().getAttribute("user");
			cid = user.getcId();
		}
		Catalog c = service.findByCidToCatalog(cid);
		// Catalog parent = c.getParent();
		/*
		 * while (parent != null) { path.add(parent); parent =
		 * parent.getParent(); }
		 */
		if (!c.getcName().equals("root")) {
			path.add(c);
		} else {
			path.removeAll(path);
		}
		//首先将path变为一个迭代器
		Iterator<Catalog> iterator = path.iterator();
		//在创建一个新的面包屑指引栏set
		path = new LinkedHashSet<Catalog>();
		while (iterator.hasNext()) {
			Catalog next = iterator.next();
			path.add(next);
			//判断文件夹的名称是否相同，是的话就跳过不加入到面包屑
			if (c.getcName().equals(next.getcName())) {
				break;
			}
		}

		request.getSession().setAttribute("path", path);

		request.setAttribute("catalog", c);
		return "f:/home.jsp";
	}

	// 创建文件夹
	public String createCatalog(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		response.setCharacterEncoding("utf-8");
		request.setCharacterEncoding("utf-8");
		service = threadList.get() == null ? new CatalogService() : threadList
				.get();
		User user = (User) request.getSession().getAttribute("user");
		if (user == null) {
			return null;
		}

		String cid = request.getParameter("cid");// 拿到他的父cid
		String cName = request.getParameter("name");
		cName = java.net.URLDecoder.decode(cName, "UTF-8");

		Catalog parent = service.findByCid(cid);

		Catalog c = new Catalog();
		// 开始封装文件夹的信息，然后存入到数据库
		c.setcId(Tool.randomId());
		if (parent != null)
			c.setParent(parent);
		c.setcName(cName);
		SimpleDateFormat simp = new SimpleDateFormat("yyyy-MM-dd");
		String newTime = simp.format(new Date()).toString();
		c.setcDate(newTime);
		c.setIsShare("0");
		c.setuId(user.getuId());
		c.setcLevel(parent.getcLevel() + 1);//将准备要创建的文件夹深度+1
		service.createCatalog(c);
		// 然后在转发到当前目录下
		/*
		 * 这有2种方案，1 是用ajax创建文件夹 2是用servlet创建 先用2 ，
		 */
		return "r:/CatalogServlet?method=myCatalog&cid=" + cid;
	}

	public String deleteCatalog(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		//线程安全地获取CatalogService
		service = threadList.get() == null ? new CatalogService() : threadList
				.get();
		PrintWriter out = response.getWriter();
		String cid = request.getParameter("cid");
		String pid = request.getParameter("pid");
		String name = request.getParameter("name");
		if (name.equals("root")) {
			out.write("<script>alert('can not delete root');window.location.href='/mydisks/CatalogServlet?method=myCatalog&cid="
					+ cid + "'</script>");
			return null;
		}
		service.deleteByCatalog(cid, this.getServletContext());
		return "r:/CatalogServlet?method=myCatalog&cid=" + pid;

	}

	public String findCatalogByPid(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		service = threadList.get() == null ? new CatalogService() : threadList
				.get();
		return null;
	}

	public String changeName(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		service = threadList.get() == null ? new CatalogService() : threadList
				.get();
		String cid = request.getParameter("cid");
		String pid = request.getParameter("pid");
		String name = request.getParameter("name");
		service.updateNameByCid(cid, name);

		return "r:/CatalogServlet?method=myCatalog&cid=" + pid;
	}

	//移动文件
	public String moveCatalog(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		service = threadList.get() == null ? new CatalogService() : threadList
				.get();
		String method = request.getParameter("do");
		String cid = null;
		String fid = null;
		if (request.getParameter("type").equals("folder")) {
			cid = request.getParameter("cId");
			cid += "-folder";
		} else if (request.getParameter("type").equals("file")) {
			fid = request.getParameter("fid");
			fid += "-file";
		}

		HttpSession session = request.getSession();
		if (method.equals("get")) {
			String id = cid == null ? fid : cid;

			User user = (User) session.getAttribute("user");
			List<Catalog> userCatalogs = service.getUserCatalog(user.getuId());
			for (Catalog catalog : userCatalogs) {

				System.out.println(catalog.getuId());
			}
			request.setAttribute("catalogList", userCatalogs);
			request.setAttribute("id", id);
		} else {

		}
		return "f:/FolderList.jsp";

	}
}
