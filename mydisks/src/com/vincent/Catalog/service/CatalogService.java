package com.vincent.Catalog.service;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;

import com.mchange.lang.StringUtils;
import com.vincent.Catalog.dao.CatalogDao;
import com.vincent.Catalog.domain.Catalog;


public class CatalogService {
	private CatalogDao cDao = new CatalogDao();
	private List<Catalog> catalogList = new ArrayList<>();
	private boolean flag = false;
	
	// 拿cid去查找他下面的一级文件夹已经文件
	public Catalog findByCidToCatalog(String cid) {
		try {
			return cDao.findByCidToCatalog(cid);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}

	}

	// 通过cid 封装catalog
	public Catalog findByCid(String cid) {
		try {
			return cDao.findByCid(cid);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public void createCatalog(Catalog c) {
		try {
			cDao.createCatalog(c);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public String cidTocf(String cid) {
		try {
			return cDao.cidTocf(cid);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public void intoCf(String cid, String cf) {
		try {
			cDao.intoCf(cid, cf);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public void deleteByCatalog(String cid, ServletContext context) {
		try {
			cDao.deleteByCatalog(cid, context);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public boolean updateNameByCid(String cid, String name) {
		try {
			cDao.updateNameByCid(cid, name);
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	public List<Catalog> tree(List<Catalog> list, String rootNaME) {

		for (Catalog catalog : list) {
			//这个是root文件夹的逻辑分支，进入去就先将root放在list的第一个元素位置
			if (catalog.getParent() == null && !flag) {
				flag = true;
				if (catalog.getcId().equals(rootNaME)) {
					catalogList.add(catalog);
				}
			}
			//所有非root的都会走这个逻辑分支，获取父节点的id，然后和传入的节点id比较
			if (catalog.getParent() != null
					&& catalog.getParent().getcId().equals(rootNaME)) {
				catalogList.add(catalog);
				tree(list, catalog.getcId());
			}
		}

		return catalogList;

	}

	public List<Catalog> getUserCatalog(String uId) {
		List<Catalog> userCatalog = null;

		try {
			Catalog root = cDao.getRoot(uId);
			userCatalog = cDao.findUserCatalog(uId);
			tree(userCatalog, root.getcId());
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return catalogList;
	}

}
