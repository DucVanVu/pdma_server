package org.pepfar.pdma.app.data.service.jpa;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.pepfar.pdma.app.Constants;
import org.pepfar.pdma.app.data.domain.*;
import org.pepfar.pdma.app.data.dto.RoleDto;
import org.pepfar.pdma.app.data.dto.UserDto;
import org.pepfar.pdma.app.data.dto.UserFilterDto;
import org.pepfar.pdma.app.data.repository.RoleRepository;
import org.pepfar.pdma.app.data.repository.UserGroupRepository;
import org.pepfar.pdma.app.data.repository.UserOrganizationRepository;
import org.pepfar.pdma.app.data.repository.UserRepository;
import org.pepfar.pdma.app.data.service.UserService;
import org.pepfar.pdma.app.utils.CommonUtils;
import org.pepfar.pdma.app.utils.ExcelUtils;
import org.pepfar.pdma.app.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.querydsl.core.types.dsl.BooleanExpression;

@Service
public class UserServiceImpl implements UserService {

	@Autowired
	private UserRepository repos;

	@Autowired
	private RoleRepository roleRepos;

	@Autowired
	private UserGroupRepository ugRepos;

	@Autowired
	private UserOrganizationRepository userOrganizationRepository;

	@Autowired
	private ApplicationContext context;

	@Override
	@Transactional(readOnly = true)
	public UserDto findById(Long id) {

		if (!CommonUtils.isPositive(id, true)) {
			return null;
		}

		User user = repos.findOne(QUser.user.id.longValue().eq(id));

		if (user != null) {
			return new UserDto(user, true);
		} else {
			return null;
		}
	}

	@Override
	@Transactional(readOnly = true)
	public UserDto findByUsername(String username) {

		if (CommonUtils.isEmpty(username)) {
			return null;
		}

		QUser q = QUser.user;

		User user = repos.findOne(q.username.eq(username));

		if (user != null) {
			return new UserDto(user, true);
		} else {
			return null;
		}
	}

	@Override
	@Transactional(readOnly = true)
	public Page<UserDto> findAllPageable(UserFilterDto filter) {

		if (filter == null) {
			filter = new UserFilterDto();
		}

		if (filter.getPageIndex() < 0) {
			filter.setPageIndex(0);
		}

		if (filter.getPageSize() <= 0) {
			filter.setPageSize(25);
		}

		List<Long> ids = null;

		if (filter.getProvinceId() != null && filter.getProvinceId().getId() != null) {
			ids = userOrganizationRepository.getListUserByProvinceId(filter.getProvinceId().getId());
		}

		QUser q = QUser.user;
		BooleanExpression be = q.id.isNotNull();

		if (!CommonUtils.isEmpty(filter.getKeyword())) {
			be = be.and(q.username.containsIgnoreCase(filter.getKeyword()).or(q.fullname
					.containsIgnoreCase(filter.getKeyword()).or(q.email.containsIgnoreCase(filter.getKeyword()))));
		}

		if (filter.getActive() != null) {
			be = be.and(q.active.eq(filter.getActive()));
		}
		if (ids != null) {
			be = be.and(q.id.in(ids));
		}

		Pageable pageable = new PageRequest(filter.getPageIndex(), filter.getPageSize(),
				new Sort(new Order(Direction.DESC, "createDate")));
		Page<User> _page = repos.findAll(be, pageable);

		List<UserDto> content = new ArrayList<>();
		_page.getContent().parallelStream().forEachOrdered(u -> {
			content.add(new UserDto(u, false));
		});

		return new PageImpl<>(content, pageable, _page.getTotalElements());
	}

	@Override
	@Transactional(readOnly = true)
	public List<UserDto> findAll(UserFilterDto filter) {
		List<UserDto> list = new ArrayList<>();

		List<Long> ids = null;
		if (filter.getProvinceId() != null && filter.getProvinceId().getId() != null) {
			ids = userOrganizationRepository.getListUserByProvinceId(filter.getProvinceId().getId());
		}

		QUser q = QUser.user;
		BooleanExpression be = q.id.isNotNull();

		if (!CommonUtils.isEmpty(filter.getKeyword())) {
			be = be.and(q.username.containsIgnoreCase(filter.getKeyword()).or(q.fullname
					.containsIgnoreCase(filter.getKeyword()).or(q.email.containsIgnoreCase(filter.getKeyword()))));
		}
		if (filter.getActive() != null) {
			be = be.and(q.active.eq(filter.getActive()));
		}
		if (ids != null) {
			be = be.and(q.id.in(ids));
		}

		repos.findAll(be, new Sort(new Order(Direction.DESC, "createDate"))).forEach(u -> {
			list.add(new UserDto(u, false));
		});

		return list;
	}

	@Override
	@Transactional(readOnly = true)
	public byte[] getProfilePhoto(String username) {
		if (CommonUtils.isEmpty(username)) {
			return null;
		}

		User user = repos.findOne(QUser.user.username.eq(username));

		if (user == null) {
			return null;
		}

		return user.getPhoto();
	}

	@Override
	@Transactional(rollbackFor = { Exception.class })
	public UserDto saveOne(UserDto dto) {

		if (CommonUtils.isNull(dto)) {
			throw new RuntimeException();
		}

		User user = null;

		if (CommonUtils.isPositive(dto.getId(), true)) {
			user = repos.findOne(dto.getId());
		}

		if (user == null) {
			user = new User();
			user.setUsername(dto.getUsername());
			user.setPassword(SecurityUtils.getHashPassword(dto.getPassword()));
			user.setJustCreated(true);
			user.setPhotoCropped(false);
		}

		user.setActive(dto.getActive());
		user.setEmail(dto.getEmail());
		user.setFullname(dto.getFullname());
		user.setPnsOnly(dto.getPnsOnly());
		user.setOpcAssistOnly(dto.getOpcAssistOnly());

		// Roles
		Set<Role> roles = new HashSet<Role>();
		if (dto.getRoles() != null) {
			dto.getRoles().parallelStream().filter(r -> (r != null) && CommonUtils.isPositive(r.getId(), true))
					.forEach(r -> {
						Role role = roleRepos.findOne(r.getId());

						if (role != null) {
							roles.add(role);
						}
					});
		}

		user.getRoles().clear();
		user.getRoles().addAll(roles);

		// UserGroup
		UserGroup ug = null;
		if (dto.getUserGroup() != null && dto.getUserGroup().getId() != null) {
			ug = ugRepos.findOne(dto.getUserGroup().getId());
		}
		user.setUserGroup(ug);

		// Save
		user = repos.save(user);

		if (user != null) {
			return new UserDto(user, false);
		} else {
			throw new RuntimeException();
		}
	}

	@Override
	@Transactional(rollbackFor = { Exception.class })
	public UserDto savePhoto(UserDto dto) {

		if (CommonUtils.isNull(dto)) {
			throw new RuntimeException();
		}

		User user = null;

		if (CommonUtils.isPositive(dto.getId(), true)) {
			user = repos.findOne(dto.getId());
		}

		if (user == null) {
			throw new RuntimeException();
		}

		user.setPhoto(dto.getPhoto());
		user.setPhotoCropped(dto.getPhotoCropped());

		// Save
		user = repos.save(user);

		if (user != null) {
			return new UserDto(user, false);
		} else {
			throw new RuntimeException();
		}
	}

	@Override
	@Transactional(readOnly = true)
	public boolean passwordMatch(UserDto dto) {

		if (dto == null || !CommonUtils.isPositive(dto.getId(), true)) {
			return false;
		}

		User user = repos.findOne(dto.getId());

		if (user != null) {
			return SecurityUtils.passwordsMatch(user.getPassword(), dto.getPassword());
		} else {
			return false;
		}
	}

	@Override
	@Transactional(rollbackFor = { Exception.class })
	public UserDto changePassword(UserDto dto) {

		if (dto == null || !CommonUtils.isPositive(dto.getId(), true) || CommonUtils.isEmpty(dto.getPassword())) {
			throw new RuntimeException();
		}

		User user = repos.findOne(dto.getId());

		if (user == null) {
			throw new RuntimeException();
		}

		// forbid changing password for 'admin' user
		if (Constants.USER_ADMIN_USERNAME.equalsIgnoreCase(user.getUsername())) {
			throw new RuntimeException("System administrator could not be modified.");
		}

		user.setPassword(SecurityUtils.getHashPassword(dto.getPassword()));

		user = repos.save(user);

		if (user == null) {
			throw new RuntimeException();
		} else {
			return new UserDto(user, false);
		}
	}

	@Override
	@Transactional(rollbackFor = { Exception.class })
	public UserDto uploadPhoto(UserDto dto) {
		return null;
	}

	@Override
	@Transactional(rollbackFor = { Exception.class })
	public void deleteMultiple(UserDto[] dtos) {

		if (dtos == null || dtos.length <= 0) {
			return;
		}

		for (UserDto dto : dtos) {
			if (dto == null || !CommonUtils.isPositive(dto.getId(), true)) {
				continue;
			}

			User entity = repos.findOne(dto.getId());

			if (entity != null) {

				// forbid deleting 'admin' user
				if (Constants.USER_ADMIN_USERNAME.equalsIgnoreCase(entity.getUsername())) {
					throw new RuntimeException("System administrator could not be deleted.");
				}

				repos.delete(entity);
			}
		}
	}

	@Override
	@Transactional(readOnly = true)
	public boolean foundDuplicateUsername(Long userId, String username) {

		if (!CommonUtils.isPositive(userId, true)) {
			userId = 0L;
		}

		QUser user = QUser.user;

		User userTemp = repos.findOne(userId);
		BooleanExpression be = user.username.equalsIgnoreCase(username);

		if (CommonUtils.isNotNull(userTemp)) {
			be = be.and(user.id.longValue().ne(userId));
		}

		return repos.findAll(be).iterator().hasNext();
	}

	@Override
	@Transactional(readOnly = true)
	public boolean foundDuplicateEmail(Long userId, String email) {

		if (!CommonUtils.isPositive(userId, true)) {
			userId = 0L;
		}

		QUser user = QUser.user;

		User userTemp = repos.findOne(userId);
		BooleanExpression be = user.email.equalsIgnoreCase(email);

		if (CommonUtils.isNotNull(userTemp)) {
			be = be.and(user.id.longValue().ne(userId));
		}

		return repos.findAll(be).iterator().hasNext();
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void resetPasswordForAllUsersExceptAdmin(String password) {
		QUser q = QUser.user;
		Iterable<User> itr = repos.findAll(q.active.isTrue().and(q.username.ne("admin")));

		itr.forEach(u -> {
			u.setPassword(SecurityUtils.getHashPassword(password));
			repos.save(u);
		});
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void createOrUpdateDemoUsers() {

		System.out.println();
		System.out.println("Creating/updating demo users.");

		InputStream inputStream = null;
		try {
			inputStream = new ClassPathResource("_demo-users.xml").getInputStream();
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		if (inputStream == null) {
			return;
		}

		SAXReader reader = new SAXReader();
		Document document = null;
		try {
			document = reader.read(inputStream);
		} catch (DocumentException e) {
			e.printStackTrace();
		}

		if (document == null) {
			return;
		}

		@SuppressWarnings("unchecked")
		List<Node> nodes = document.selectNodes("//system-users/user");

		nodes.forEach(node -> {
			User user = repos.findOne(QUser.user.username.eq(node.valueOf("@username")));

			if (user == null) {
				user = new User();
				user.setJustCreated(true);
				user.setActive(true);
				user.setPhotoCropped(false);
				user.setUsername(node.valueOf("@username"));
				user.setEmail(node.valueOf("@email"));
				user.setFullname(node.valueOf("@full-name"));

				@SuppressWarnings("unchecked")
				List<Node> roleNodes = node.selectNodes("roles/role");
				Set<Role> roles = new HashSet<>();

				roleNodes.forEach(rnode -> {
					Role role = roleRepos.findOne(QRole.role.name.equalsIgnoreCase(rnode.valueOf("@name")));
					if (role != null) {
						roles.add(role);
					}
				});

				user.getRoles().clear();
				user.getRoles().addAll(roles);
			}

			user.setPassword(SecurityUtils.getHashPassword(node.valueOf("@password")));

			repos.save(user);
			System.out.print(".");

		});

		System.out.println();
	}

	@Override
	public Workbook exportUser(UserFilterDto filter) {
		Workbook blankBook = new XSSFWorkbook();
		blankBook.createSheet();
		List<UserDto> result = this.findAll(filter);
		if (result == null) {
			return blankBook;
		} else {
			Workbook wbook = null;
			try (InputStream template = context.getResource("classpath:templates/user-list.xlsx").getInputStream()) {
				wbook = new XSSFWorkbook(template);
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (wbook == null) {
				return blankBook;
			}
			int rowIndex = 7;
			int colIndex = 0;

			Row row = null;
			Cell cell = null;
			Sheet sheet = wbook.getSheetAt(0);
			Font font = sheet.getWorkbook().createFont();
		    font.setFontName("Times New Roman"); 
			int seq = 0;
			CellStyle cellStyle = wbook.createCellStyle();
			ExcelUtils.setBorders4Style(cellStyle);
			cellStyle.setWrapText(false);
			cellStyle.setAlignment(HorizontalAlignment.LEFT);
			cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
			cellStyle.setFont(font);

			if (filter.getActive() != null) {
				row = sheet.getRow(2);
				cell = row.createCell(2);
				cell.setCellValue(filter.getActive() ? "Đã kích hoạt" : "Chưa kích hoạt");
			}

			if (filter.getProvinceId() != null) {
				row = sheet.getRow(3);
				cell = row.createCell(2);
				cell.setCellValue(filter.getProvinceId().getName());
			}

			if (filter.getKeyword() != null) {
				row = sheet.getRow(4);
				cell = row.createCell(2);
				cell.setCellValue(filter.getKeyword());
			}

			for (UserDto user : result) {
				row = sheet.createRow(rowIndex++);

				cell = row.createCell(colIndex++);
				cell.setCellValue(seq += 1);
				cell.setCellStyle(cellStyle);

				cell = row.createCell(colIndex++);
				cell.setCellStyle(cellStyle);
				if (user.getFullname() != null) {
					cell.setCellValue(user.getFullname());
				} else {
					cell.setCellValue("");
				}

				cell = row.createCell(colIndex++);
				cell.setCellStyle(cellStyle);
				if (user.getUsername() != null) {
					cell.setCellValue(user.getUsername());
				} else {
					cell.setCellValue("");
				}

				cell = row.createCell(colIndex++);
				cell.setCellStyle(cellStyle);
				if (user.getEmail() != null) {
					cell.setCellValue(user.getEmail());
				} else {
					cell.setCellValue("");
				}

				cell = row.createCell(colIndex++);
				cell.setCellStyle(cellStyle);
				if (user.getRoles() != null) {
					String role = "";
					for (RoleDto roleDto : user.getRoles()) {
						if (roleDto.getName() != null) {
							if (role.length() > 0) {
								role += ", " + roleDto.getName();
							} else {
								role += roleDto.getName();
							}
						} else {
							role += "";
						}
					}
					cell.setCellValue(role);
				} else {
					cell.setCellValue("");
				}

				cell = row.createCell(colIndex++);
				cell.setCellStyle(cellStyle);
				if (user.getActive() != null) {
					cell.setCellValue(user.getActive() ? "Đã kích hoạt" : "Chưa kích hoạt");
				} else {
					cell.setCellValue("");
				}

				colIndex = 0;

			}
			for (int i = 0; i < 6; i++) {
				sheet.autoSizeColumn(i);
			}
			return wbook;
		}
	}

}
