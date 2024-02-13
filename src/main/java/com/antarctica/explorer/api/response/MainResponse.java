package com.antarctica.explorer.api.response;

import com.antarctica.explorer.api.model.CruiseLine;
import java.util.Map;

public record MainResponse(Map<String, CruiseLine> cruiseLines, ExpeditionPageResponse expeditions) {}
