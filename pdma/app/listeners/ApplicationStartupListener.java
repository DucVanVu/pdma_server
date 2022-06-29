package org.pepfar.pdma.app.listeners;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.hibernate.Session;
import org.hibernate.transform.AliasToBeanResultTransformer;
import org.pepfar.pdma.app.Constants;
import org.pepfar.pdma.app.data.dto.AdminUnitDto;
import org.pepfar.pdma.app.data.dto.DictionaryDto;
import org.pepfar.pdma.app.data.dto.HIVConfirmLabDto;
import org.pepfar.pdma.app.data.dto.LocationDto;
import org.pepfar.pdma.app.data.dto.OrganizationDto;
import org.pepfar.pdma.app.data.dto.PreferencesDto;
import org.pepfar.pdma.app.data.dto.PreventionChartDetailDto;
import org.pepfar.pdma.app.data.dto.RegimenDto;
import org.pepfar.pdma.app.data.dto.RoleDto;
import org.pepfar.pdma.app.data.dto.ServiceDto;
import org.pepfar.pdma.app.data.dto.ServiceOrganizationDto;
import org.pepfar.pdma.app.data.dto.UserDto;
import org.pepfar.pdma.app.data.opcassistimport.Importer;
import org.pepfar.pdma.app.data.service.AdminUnitService;
import org.pepfar.pdma.app.data.service.DictionaryService;
import org.pepfar.pdma.app.data.service.HIVConfirmLabService;
import org.pepfar.pdma.app.data.service.OrganizationService;
import org.pepfar.pdma.app.data.service.PreferencesService;
import org.pepfar.pdma.app.data.service.RegimenService;
import org.pepfar.pdma.app.data.service.RoleService;
import org.pepfar.pdma.app.data.service.ServiceOrganizationService;
import org.pepfar.pdma.app.data.service.ServiceService;
import org.pepfar.pdma.app.data.service.UserService;
import org.pepfar.pdma.app.data.types.DictionaryType;
import org.pepfar.pdma.app.utils.CommonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 
 * Listen to application startup and populate necessary data in order for the
 * system to work.
 * 
 * @author NGUYEN TUAN ANH
 *
 */
//@Transactional
@Component
public class ApplicationStartupListener implements ApplicationListener<ContextRefreshedEvent>, InitializingBean {

	private static boolean eventFired = false;

	@Autowired
	private Environment env;

	@Autowired
	private RoleService roleService;

	@Autowired
	private UserService userService;

	@Autowired
	private ServiceService serviceService;

	@Autowired
	private AdminUnitService auService;

	@Autowired
	private OrganizationService orgService;

	@Autowired
	private ServiceOrganizationService soService;

	@Autowired
	private DictionaryService dicService;

	@Autowired
	private RegimenService regimenService;

	@Autowired
	private HIVConfirmLabService labService;

	@Autowired
	private PreferencesService prefService;

	@Autowired
	private ApplicationContext context;

	@Autowired
	private Importer importer;
	
	@Autowired
	private EntityManager manager;

	private static final Logger logger = LoggerFactory.getLogger(ApplicationStartupListener.class);

	@Transactional
	public void onApplicationEvent(ContextRefreshedEvent event) {

		// Only process the first time the event is fired (meaning when the
		// application context is sucessfully loaded)
		if (eventFired) {
			return;
		}

		// The application is started
		logger.info("Application started.");
		System.out.println("Application started.");

		eventFired = true;

		boolean createDemoUsers = Boolean.valueOf(env.getProperty("spring.data.create-demo-users"));
		boolean prePopulate = Boolean.valueOf(env.getProperty("spring.data.pre-populate"));
		boolean correctOccupation = Boolean.valueOf(env.getProperty("spring.data.correct-occupation"));
		boolean correctArvRegimenInAppointments = Boolean
				.valueOf(env.getProperty("spring.data.correct-app-arv-regimen"));

		if (createDemoUsers) {
			userService.createOrUpdateDemoUsers();
		}

//		try {
//			System.out.println("-----------------update UUID------------------");
//			updateUUID();
//		}
//		catch (Exception ex) {
//			ex.printStackTrace();
//		}
		
		// --------------
//		try {

//			createRegimens();

		/**
			 * @formatter:off
			 */
//			createDictionaries(new String[] {
//					"_dic-usedshiservices.xml"
//					"_dic-hiv-status.xml", 
//					"_dic-hiv-testing-results.xml",
//					"_dic-reasons-cd4-testing.xml",
//					"_dic-stop-mmt-reasons.xml",
//					"_dic-vl-funding-sources.xml",
//					"_dic-vl-testing-reasons.xml"
//					});
			/**
			 * @formatter:on
			 */
//		} catch (Exception ex) {
//			ex.printStackTrace();
//		}
		// --------------

		if (correctArvRegimenInAppointments) {
			importer.updateAppointmentARVRegimenData();
		}

		if (correctOccupation) {
			importer.clinicalStageAndOccupationUpdate();
		}

		if (!prePopulate) {
			return;
		}

		// Other organization for patient referral purpose
		createOtherOrganization();

		// GSO codes for admin units
		auService.updateGsoCode();

		// Preferences for PNS Assessment
		createInitialPreferences();

		try {
			// Default roles and users
			createRoles();
			createUsers();

			// Dictionaries
			createDictionaries(null);

			// Services
			createServices();

			// Admin units & organizations
			createAdminUnits();
			createOrganizations();

			// Regimens
			createRegimens();

			// Confirm labs
			createLabs();

		} catch (XMLStreamException ex) {
			ex.printStackTrace();
		} catch (IOException ex) {
			ex.printStackTrace();
		} catch (DocumentException ex) {
			ex.printStackTrace();
		}
	}

	public void afterPropertiesSet() throws Exception {
		// TODO: Make sure all the beans are wired to the context
	}

	// ---------------------------------------
	// Private methods
	// ---------------------------------------

	@SuppressWarnings("unused")
	@Deprecated
	private void updateLocationsForOrganizationAndHIVConfirmLab() {
//		importer.updateCaseAddresses("2019!2020");
	}

	private void createInitialPreferences() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

		PreferencesDto prefDto = prefService.findByName(Constants.PROP_PNS_BASELINE_TO_DATE);
		if (prefDto == null) {
			LocalDateTime dt = LocalDateTime.of(2020, 6, 7, 23, 59, 00);
			String ds = sdf.format(CommonUtils.fromLocalDateTime(dt));

			prefDto = new PreferencesDto();
			prefDto.setName(Constants.PROP_PNS_BASELINE_TO_DATE);
			prefDto.setValue(ds);

			prefService.saveOne(prefDto);
		}

		prefDto = prefService.findByName(Constants.PROP_PNS_POST_FROM_DATE);
		if (prefDto == null) {
			LocalDateTime dt = LocalDateTime.of(2020, 9, 1, 00, 00, 00);
			String ds = sdf.format(CommonUtils.fromLocalDateTime(dt));

			prefDto = new PreferencesDto();
			prefDto.setName(Constants.PROP_PNS_POST_FROM_DATE);
			prefDto.setValue(ds);

			prefService.saveOne(prefDto);
		}

		prefDto = prefService.findByName(Constants.PROP_PNS_POST_TO_DATE);
		if (prefDto == null) {
			LocalDateTime dt = LocalDateTime.of(2020, 9, 30, 23, 59, 00);
			String ds = sdf.format(CommonUtils.fromLocalDateTime(dt));

			prefDto = new PreferencesDto();
			prefDto.setName(Constants.PROP_PNS_POST_TO_DATE);
			prefDto.setValue(ds);

			prefService.saveOne(prefDto);
		}
	}

	private void createOtherOrganization() {
		OrganizationDto org = orgService.findByCode(Constants.CODE_ORGANIZATION_OTHER);

		if (org != null) {
			return;
		}

		org = new OrganizationDto();
		org.setActive(true);
		org.setAddress(null);
		org.setCode(Constants.CODE_ORGANIZATION_OTHER);
		org.setConfirmLab(true);
		org.setDescription("This is a default system generated record. Please do not modify or delete.");
		org.setHtsSite(true);
		org.setLevel(1);
		org.setName("Cơ sở khác");
		org.setOpcSite(true);
		org.setPepfarSite(false);
		org.setPnsSite(true);
		org.setPrepSite(true);

		orgService.saveOne(org);
	}

	private void createLabs() throws XMLStreamException, DocumentException, IOException {
		System.out.println();
		System.out.println("Creating labs.");

		InputStream inputFile = context.getResource("classpath:_labs.xml").getInputStream();
		SAXReader reader = new SAXReader();
		Document document = reader.read(inputFile);

		@SuppressWarnings("unchecked")
		List<Node> nodes = document.selectNodes("//labs/lab");
		AdminUnitDto countryDto = auService.findByCode("country_1");

		nodes.forEach(node -> {
			HIVConfirmLabDto lab = labService.findByCode(node.valueOf("@code"));

			if (lab != null) {
				return;
			}

			AdminUnitDto provinceDto = auService.findByCode(node.valueOf("@p-code"));
			LocationDto addressDto = new LocationDto();
			addressDto.setCountry(countryDto);
			addressDto.setProvince(provinceDto);

			lab = new HIVConfirmLabDto();
			lab.setCode(node.valueOf("@code"));
			lab.setName(node.valueOf("@name"));
			lab.setAddress(addressDto);

			labService.saveOne(lab);

			System.out.print(".");
		});
	}

	/**
	 * Regimens
	 * 
	 * @throws XMLStreamException
	 * @throws DocumentException
	 * @throws IOException
	 */
	private void createRegimens() throws XMLStreamException, DocumentException, IOException {
		System.out.println();
		System.out.println("Creating regimens.");

		InputStream inputFile = context.getResource("classpath:_regimens.xml").getInputStream();
		SAXReader reader = new SAXReader();
		Document document = reader.read(inputFile);

		@SuppressWarnings("unchecked")
		List<Node> nodes = document.selectNodes("//regimens/regimen");

		nodes.forEach(node -> {
			RegimenDto regimenDto = regimenService.findByShortName(node.valueOf("@short-name"));

			if (regimenDto == null) {
				DictionaryDto disease = dicService.findByCode(node.valueOf("@disease-code"));

				if (disease == null) {
					return;
				}

				regimenDto = new RegimenDto();
				regimenDto.setDisease(disease);

				if (!CommonUtils.isEmpty(node.valueOf("@line"))) {
					regimenDto.setLine(Integer.valueOf(node.valueOf("@line")));
				} else {
					regimenDto.setLine(1);
				}
				regimenDto.setName(node.valueOf("@name"));
				regimenDto.setShortName(node.valueOf("@short-name"));

				regimenService.saveOne(regimenDto);

				System.out.print(".");
			}
		});
	}

	/**
	 * Dictionaries
	 * 
	 * @throws XMLStreamException
	 */
	private void createDictionaries(String[] input) throws XMLStreamException {
		System.out.println();
		System.out.println("Creating dictionaries.");

		String[] files = null;

		if (!CommonUtils.isEmpty(input)) {
			files = input;
		} else {
			files = allDictionaries();
		}

		if (CommonUtils.isEmpty(files)) {
			return;
		}

		Stream.of(files).forEach(filename -> {
			InputStream inputFile = null;
			try {
				inputFile = context.getResource("classpath:" + filename).getInputStream();
			} catch (IOException e) {
				e.printStackTrace();
			}

			SAXReader reader = new SAXReader();
			Document document = null;
			try {
				document = reader.read(inputFile);
			} catch (DocumentException e) {
				e.printStackTrace();
			}

			@SuppressWarnings("unchecked")
			List<Node> nodes = document.selectNodes("//dictionary/entry");

			nodes.forEach(node -> {
				DictionaryDto dto = dicService.findByCode(node.valueOf("@code"));

				if (dto == null) {
					dto = new DictionaryDto();
					dto.setActive(Boolean.valueOf(node.valueOf("@active")));
					dto.setCode(node.valueOf("@code"));
					dto.setOrder(Integer.valueOf(node.valueOf("@order")));
					dto.setType(DictionaryType.valueOf(node.valueOf("@type").toUpperCase()));
					dto.setValue(node.valueOf("@value"));
					dto.setValueEn(node.valueOf("@value-en"));

					dto = dicService.saveOne(dto);
				}

				System.out.print("."); // logging to the console
			});
		});
	}

	private String[] allDictionaries() {
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		return new File(loader.getResource("./").getPath()).list(new FilenameFilter() {

			@Override
			public boolean accept(File dir, String name) {
				return name.startsWith("_dic") && name.endsWith(".xml");
			}

		});
	}

	/**
	 * Organizations
	 * 
	 * @throws XMLStreamException
	 * @throws DocumentException
	 * @throws IOException
	 */
	private void createOrganizations() throws XMLStreamException, DocumentException, IOException {

		System.out.println();
		System.out.println("Creating organizations.");

		// Check if this table is already populated
		if (orgService.count() > 0) {
			return;
		}

		InputStream inputFile = context.getResource("classpath:_organizations.xml").getInputStream();
		SAXReader reader = new SAXReader();
		Document document = reader.read(inputFile);

		@SuppressWarnings("unchecked")
		List<Node> nodes = document.selectNodes("//organizations/organization");

		nodes.forEach(node -> {
			OrganizationDto orgDto = new OrganizationDto();
			orgDto.setName(node.valueOf("@name"));
			orgDto.setActive(true);
			orgDto.setPepfarSite(false);

			Node addressNode = node.selectSingleNode("address");

			AdminUnitDto countryDto = new AdminUnitDto();
			AdminUnitDto provinceDto = new AdminUnitDto();
			AdminUnitDto districtDto = new AdminUnitDto();

			countryDto.setCode(addressNode.valueOf("@c-code"));
			provinceDto.setCode(addressNode.valueOf("@p-code"));
			districtDto.setCode(addressNode.valueOf("@d-code"));

			LocationDto addressDto = new LocationDto();
			addressDto.setCountry(countryDto);
			addressDto.setProvince(provinceDto);
			addressDto.setDistrict(districtDto);
			addressDto.setStreetAddress(addressNode.valueOf("@street"));

			orgDto.setAddress(addressDto);

			orgDto = orgService.saveOne(orgDto);

			if (orgDto != null && CommonUtils.isPositive(orgDto.getId(), true)) {

				@SuppressWarnings("unchecked")
				List<Node> serviceNodes = node.selectNodes("services/service");
				List<ServiceDto> services = new ArrayList<>();

				if (serviceNodes != null) {
					serviceNodes.forEach(serviceNode -> {
						ServiceDto serviceDto = serviceService.findByCode(serviceNode.valueOf("@code"));

						if (serviceDto != null) {
							services.add(serviceDto);
						}
					});
				}

				for (ServiceDto serviceDto : services) {
					ServiceOrganizationDto so = new ServiceOrganizationDto();
					so.setOrganization(orgDto);
					so.setService(serviceDto);
					so.setActive(true);
					so.setStartDate(CommonUtils.hanoiTodayStart().minusYears(10));
					so.setEndDate(CommonUtils.hanoiTodayEnd().plusYears(50));
					so.setEndingReason(null);

					so = soService.saveOne(so);
				}
			}

			System.out.print(".");

		});
	}

	/**
	 * Admin units
	 * 
	 * @throws XMLStreamException
	 * @throws DocumentException
	 * @throws IOException
	 */
	private void createAdminUnits() throws XMLStreamException, DocumentException, IOException {

		System.out.println();
		System.out.println("Creating admin units.");

		InputStream inputFile = context.getResource("classpath:_admin-units.xml").getInputStream();
		SAXReader reader = new SAXReader();
		Document document = reader.read(inputFile);

		@SuppressWarnings("unchecked")
		List<Node> nodes = document.selectNodes("//admin-units/admin-unit");

		nodes.forEach(node -> {

			AdminUnitDto country = new AdminUnitDto();
			AdminUnitDto province = new AdminUnitDto();
			AdminUnitDto district = new AdminUnitDto();
			AdminUnitDto commune = new AdminUnitDto();

			country.setCode(node.valueOf("@c-code"));
			country.setName(node.valueOf("@c-name"));
			country.setLevel(1);
			country.setVoided(false);
			// Check if country exists and create if not
			AdminUnitDto dto = auService.findByCode(country.getCode());
			if (dto == null) {
				country = auService.saveOne(country);
			} else {
				country = new AdminUnitDto(dto.toEntity());
			}

			province.setCode(node.valueOf("@p-code"));
			province.setName(node.valueOf("@p-name"));
			province.setLevel(2);
			province.setVoided(false);
			// Check if province exists and create if not
			dto = auService.findByCode(province.getCode());
			if (dto == null) {
				province.setParent(country);
				province = auService.saveOne(province);
			} else {
				province = new AdminUnitDto(dto.toEntity());
			}

			district.setCode(node.valueOf("@d-code"));
			district.setName(node.valueOf("@d-name"));
			district.setLevel(3);
			district.setVoided(false);
			// Check if district exists and create if not
			dto = auService.findByCode(district.getCode());
			if (dto == null) {
				district.setParent(province);
				district = auService.saveOne(district);
			} else {
				district = new AdminUnitDto(dto.toEntity());
			}

			commune.setCode(node.valueOf("@cm-code"));
			commune.setName(node.valueOf("@cm-name"));
			commune.setLevel(4);
			commune.setVoided(false);
			// Check if commune exists and create if not
			dto = auService.findByCode(commune.getCode());
			if (dto == null) {
				commune.setParent(district);
				commune = auService.saveOne(commune);
			}

			System.out.print(".");

		});
	}

	/**
	 * Services
	 * 
	 * @throws XMLStreamException
	 * @throws DocumentException
	 * @throws IOException
	 */
	private void createServices() throws XMLStreamException, DocumentException, IOException {

		System.out.println();
		System.out.println("Creating services.");

		InputStream inputFile = context.getResource("classpath:_services.xml").getInputStream();
		SAXReader reader = new SAXReader();
		Document document = reader.read(inputFile);

		@SuppressWarnings("unchecked")
		List<Node> nodes = document.selectNodes("//services/service");

		nodes.forEach(node -> {

			ServiceDto dto = serviceService.findByCode(node.valueOf("@code"));
			if (dto == null) {
				dto = new ServiceDto();
				dto.setCode(node.valueOf("@code"));
				dto.setName(node.valueOf("@name"));

				serviceService.saveOne(dto);
			}

			System.out.print(".");
		});
	}

	/**
	 * Create all users as defined in the XML file
	 * 
	 * @throws XMLStreamException
	 * @throws DocumentException
	 * @throws IOException
	 */
	private void createUsers() throws XMLStreamException, DocumentException, IOException {

		System.out.println();
		System.out.println("Creating users.");

		InputStream inputFile = context.getResource("classpath:_system-users.xml").getInputStream();
		SAXReader reader = new SAXReader();
		Document document = reader.read(inputFile);

		@SuppressWarnings("unchecked")
		List<Node> nodes = document.selectNodes("//system-users/user");

		nodes.forEach(node -> {
			UserDto userDto = new UserDto();
			userDto.setJustCreated(true);
			userDto.setActive(true);
			userDto.setPhotoCropped(false);
			userDto.setUsername(node.valueOf("@username"));
			userDto.setPassword(node.valueOf("@password"));
			userDto.setEmail(node.valueOf("@email"));
			userDto.setFullname(node.valueOf("@full-name"));

			@SuppressWarnings("unchecked")
			List<Node> roleNodes = node.selectNodes("roles/role");
			roleNodes.forEach(rnode -> {
				RoleDto role = new RoleDto();
				role.setName(rnode.valueOf("@name"));

				userDto.getRoles().add(role);
			});

			createUserIfNotExist(userDto);

			System.out.print(".");
		});
	}

	/**
	 * Create a user if not exists
	 * 
	 * @param dto
	 */
	private void createUserIfNotExist(UserDto dto) {

		if (CommonUtils.isNotNull(userService.findByUsername(dto.getUsername()))) {
			return;
		}

		if (dto.getRoles() != null) {
			Set<RoleDto> roles = new HashSet<RoleDto>();

			dto.getRoles().forEach(r -> {
				RoleDto roleDto = roleService.findOne(r.getName());

				if (roleDto != null) {
					roles.add(roleDto);
				}
			});

			dto.getRoles().addAll(roles);
		}

		try {
			userService.saveOne(dto);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Read xml file and create all system roles
	 * 
	 * @throws XMLStreamException
	 */
	private void createRoles() throws XMLStreamException {

		System.out.println("Creating roles.");

		List<String> roleNames = new ArrayList<>();

		XMLInputFactory inputFactory = XMLInputFactory.newInstance();
		InputStream in = getClass().getClassLoader().getResourceAsStream("_system-roles.xml");
		XMLStreamReader streamReader = inputFactory.createXMLStreamReader(in, "UTF-8");

		while (streamReader.hasNext()) {

			streamReader.next();

			if (streamReader.isStartElement()) {

				int attributes = streamReader.getAttributeCount();

				for (int i = 0; i < attributes; i++) {
					String name = streamReader.getAttributeLocalName(i);
					String value = streamReader.getAttributeValue(i);

					if (name.equals("name") && !CommonUtils.isEmpty(value)) {
						roleNames.add(value);
					}
				}
			}

			System.out.print(".");
		}

		streamReader.close();

		for (String roleName : roleNames) {
			createRoleIfNotExist(roleName);

			System.out.print(".");
		}
	}

	/**
	 * Create a role of name roleName if not exists yet
	 * 
	 * @param roleName
	 */
	private void createRoleIfNotExist(String roleName) {

		if (CommonUtils.isEmpty(roleName)) {
			return;
		}

		RoleDto role = roleService.findOne(roleName);

		if (CommonUtils.isNotNull(role)) {
			return;
		}

		if (role == null) {
			role = new RoleDto();
			role.setName(roleName);
		}

		try {
			roleService.saveOne(role);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
//	@Transactional
	private void updateUUID() {
//		Query q = manager.unwrap(Session.class).createSQLQuery("");
//		q.executeUpdate();
		int person = manager.createNativeQuery("UPDATE tbl_person SET uuid = (SELECT UUID()) WHERE uuid is NULL OR UUID=''").executeUpdate();
		System.out.println("UPDATE tbl_person "+person+" rows ");
		int pnsCase = manager.createNativeQuery("UPDATE tbl_pns_case SET uuid = (SELECT UUID()) WHERE uuid is null OR UUID=''").executeUpdate();
		System.out.println("UPDATE tbl_pns_case "+pnsCase+" rows ");
		int peCase = manager.createNativeQuery("UPDATE tbl_pe_case SET uuid = (SELECT UUID()) WHERE uuid is null OR UUID=''").executeUpdate();
		System.out.println("UPDATE tbl_pe_case "+peCase+" rows ");
	}
}
