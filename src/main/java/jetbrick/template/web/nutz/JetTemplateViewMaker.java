package jetbrick.template.web.nutz;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jetbrick.template.JetContext;
import jetbrick.template.JetTemplate;
import jetbrick.template.web.JetWebContext;
import jetbrick.template.web.JetWebEngineLoader;

import org.nutz.ioc.Ioc;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.mvc.Mvcs;
import org.nutz.mvc.View;
import org.nutz.mvc.ViewMaker;
import org.nutz.mvc.view.AbstractPathView;

/**
 * nutz与JetTemplate集成
 * <p/>
 * 使用方法
 * 
 * @author wendal(wendal1985@gmail.com)
 */
public class JetTemplateViewMaker implements ViewMaker {

	private static Log log = Logs.get();

	private String viewType;

	public JetTemplateViewMaker() {
		JetWebEngineLoader.setServletContext(Mvcs.getServletContext());
		viewType = JetWebEngineLoader.getTemplateSuffix().substring(1);
		log.debugf("using '%s' as JetTemplateView TypeName", viewType);
	}

	public View make(Ioc ioc, String type, final String value) {
		if (!viewType.equalsIgnoreCase(type))
			return null;
		return new AbstractPathView(value) {
			public void render(HttpServletRequest req,
					HttpServletResponse resp, Object obj) throws Throwable {
				JetContext context = new JetWebContext(req, resp);
				if (obj != null)
					context.put("obj", obj);
				JetTemplate template = JetWebEngineLoader.getJetEngine().getTemplate(evalPath(req, obj));
				template.render(context, resp.getOutputStream());
			}
		};
	}
}
