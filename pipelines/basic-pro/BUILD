COMPILE_DEPS = CORE_DEPS + [
    "@minimal_json//jar",
    "//protocols/p4runtime/model:onos-protocols-p4runtime-model",
    "//protocols/p4runtime/api:onos-protocols-p4runtime-api",
    "//apps/inbandtelemetry/api:onos-apps-inbandtelemetry-api",
    "//pipelines/basic:onos-pipelines-basic",
    "//drivers/default:onos-drivers-default",
]

BUNDLES = [
    "//pipelines/basic-pro:onos-pipelines-basic-pro",
    "//apps/inbandtelemetry/api:onos-apps-inbandtelemetry-api",
]

osgi_jar(
    deps = COMPILE_DEPS,
)

onos_app(
    app_name = "org.onosproject.pipelines.basic-pro",
    category = "Pipeline",
    description = "Test pipeline",
    included_bundles = BUNDLES,
    required_apps = [
        "org.onosproject.drivers.p4runtime",
        "org.onosproject.pipelines.basic",
    ],
    title = "Basic-Pro Pipelines",
    url = "http://onosproject.org",
)
