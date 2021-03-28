package academy.utils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import com.ptc.core.components.forms.DynamicRefreshInfo;
import com.ptc.core.components.forms.DynamicRefreshInfo.Action;
import com.ptc.core.components.forms.FormProcessingStatus;
import com.ptc.core.components.forms.FormResult;
import com.ptc.netmarkets.model.NmOid;
import com.ptc.netmarkets.util.beans.NmCommandBean;

import wt.fc.ObjectReference;
import wt.fc.Persistable;
import wt.fc.WTObject;
import wt.fc.WTReference;
import wt.fc.collections.WTHashSet;
import wt.fc.collections.WTSet;
import wt.util.WTException;
import wt.util.WTRuntimeException;

public abstract class WizardUtils {
	private static final Logger LOGGER = Logger.getLogger(WizardUtils.class.getName());
	private static final String SELECTED_SESSION_KEY = "multiSelect";
	
	public static FormResult setRefreshInfoUpdate(Persistable obj, Persistable objLocation, Action action, FormResult result) {
		DynamicRefreshInfo  dri = new DynamicRefreshInfo(obj, objLocation, action);
		result.addDynamicRefreshInfo(dri);
		return result;
	}
	
	public static DynamicRefreshInfo getRefreshInfo(Persistable obj, Persistable objLocation, Action action) {
		return new DynamicRefreshInfo(obj, objLocation, action);
	}
	
	public static FormResult getSuccessfullFormResult() {
		FormResult result = new FormResult();
		result.setStatus(FormProcessingStatus.SUCCESS);
		return result;
	}
	
	public static WTSet getSelectedInOpener(NmCommandBean commandBean) throws WTException {
		List<NmOid> nmOidList = new ArrayList<>();
		Collection<NmOid> selectedInOpener = commandBean.getNmOidSelectedInOpener();
		if (selectedInOpener == null || selectedInOpener.isEmpty()) {
			Collection<?> multiSelect = (Collection<?>) commandBean.getSessionBean().getStorage().get(SELECTED_SESSION_KEY);
			
			if (multiSelect == null || multiSelect.isEmpty()) {
				multiSelect = (Collection<?>) commandBean.getMap().get(SELECTED_SESSION_KEY);
			}

			if (multiSelect != null && !multiSelect.isEmpty()) {
				for (Object object : multiSelect) {
					nmOidList.add(NmCommandBean.getOidFromObject(object));
				}
			}
		} else {
			nmOidList.addAll(selectedInOpener);
		}

		return getObjectsSetFromNmOidList(nmOidList);
	}
	
	public static WTSet getObjectsSetFromNmOidList(List<NmOid> nmOidList) throws WTException {
		WTSet result = new WTHashSet();
		if (null != nmOidList) {
			for (NmOid nmOid : nmOidList) {
				result.add(ObjectReference.newObjectReference(nmOid.getOidObject()));
			}
		}
		if (!result.isEmpty()) {
			result.inflate();
		}
		return result;
	}
	
	public static WTObject getContextObject(NmCommandBean commandBean) throws WTRuntimeException, WTException {
		WTReference reference = commandBean.getActionOid().getWtRef();
		if(reference != null){
			return (WTObject) reference.getObject();
		}
		LOGGER.error("Unable to find context object for Command Bean");
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public static String getOids(NmCommandBean commandBean, String tableID) throws WTException{
		List<NmOid> items = commandBean.getAddedItemsByName(tableID);
		items.addAll(commandBean.getInitialItemsByName(tableID));
		items.addAll(commandBean.getSelectedOidForPopup());
		items.removeAll(commandBean.getRemovedItemsByName(tableID));
		
		StringBuilder builder = new StringBuilder(commandBean.getActionOid().getReferenceString());
		
		Iterator<NmOid> iterator = items.iterator();
		while(iterator.hasNext()){
			NmOid item = iterator.next();
			builder.append(item.getReferenceString());
			
			if(iterator.hasNext()){
				builder.append(",");
			}
		}
		
		return builder.toString();
	}
}


