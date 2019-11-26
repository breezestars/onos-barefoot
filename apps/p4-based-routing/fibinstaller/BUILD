COMPILE_DEPS = CORE_DEPS + [
    "//apps/routing-api:onos-apps-routing-api",
    "//apps/route-service/api:onos-apps-route-service-api",
]

TEST_DEPS = TEST_ADAPTERS + [
]

osgi_jar_with_tests(
    deps = COMPILE_DEPS,
)

BUNDLES = [
    "//apps/p4-based-routing/fibinstaller:onos-apps-p4-based-routing-fibinstaller",
    "//apps/routing-api:onos-apps-routing-api",
]

onos_app(
    app_name = "org.onosproject.p4basedrouting.fibinstaller",
    category = "Traffic Engineering",
    description = "Installs routing rules into switches",
    included_bundles = BUNDLES,
    required_apps = ["org.onosproject.route-service"],
    title = "FIB Installer",
    url = "http://onosproject.org",
)
