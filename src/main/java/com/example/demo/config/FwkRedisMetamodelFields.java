// package com.example.demo.config;

// import java.util.Arrays;
// import java.util.HashMap;
// import java.util.List;
// import java.util.Map;
// import java.util.stream.Collectors;

// import org.springframework.context.annotation.Scope;
// import org.springframework.stereotype.Component;

// import com.redis.om.spring.metamodel.MetamodelField;
// import com.redis.om.spring.metamodel.MetamodelUtils;

// @Component
// @Scope("singleton")
// public class FwkRedisMetamodelFields {

// 	private static Map<Class<? extends DemoBaseEntity>, Map<String, MetamodelField<? extends DemoBaseEntity, ?>>>  baseEntities_MetamodelFields = new HashMap<>();

// 	private <E extends DemoBaseEntity> void populateMetamodelFieldsForEntity(Class<E> entityClass) throws Exception {
		
// 		Map<String, MetamodelField<? extends DemoBaseEntity, ?>> entityMetamodelFields = baseEntities_MetamodelFields.get(entityClass);
// 		if (entityMetamodelFields == null) {
			
// 			List<String> properties = Arrays.asList(entityClass.getDeclaredFields()).stream().map(field -> field.getName()).collect(Collectors.toList());
// 			List<MetamodelField<?, ?>> metamodelFields = MetamodelUtils.getMetamodelFieldsForProperties(entityClass, properties);
// 			if (metamodelFields != null && !metamodelFields.isEmpty()) {
// 				entityMetamodelFields = new HashMap<>();
// 				for(MetamodelField<?, ?> metamodelField : metamodelFields) {
// 					entityMetamodelFields.put(metamodelField.getSearchFieldAccessor().getField().getName(), (MetamodelField<E, ?>) metamodelField);			
// 				}
// 				entityMetamodelFields.put(RTWBRedisCacheConstants.ID_FIELD_STR, (MetamodelField<E, ?>) MetamodelUtils.getMetamodelForIdField(entityClass));
// 				baseEntities_MetamodelFields.put(entityClass, entityMetamodelFields);				
// 			} else {
// 				throw new Exception("Entity : " + entityClass + " is not valid Redis Entity");
// 			}
// 		}
// 	}
	
// 	public <E extends DemoBaseEntity> MetamodelField<E, ?>[] getMetamodelFields(Class<E> entityClass, String[] fields) throws Exception {
		
// 		populateMetamodelFieldsForEntity(entityClass);
		
// 		MetamodelField<E, ?>[] returnMetamodelFields = new MetamodelField[fields.length];
		
// 		// entityMetamodels cannot be null as its already populated, if entityClass is invalid redis entity then exception will be thorwn from populate method
// 		Map<String, MetamodelField<? extends DemoBaseEntity, ?>> entityMetamodels = baseEntities_MetamodelFields.get(entityClass);

// 		for(int i=0; i<fields.length; i++) {
// 			MetamodelField<? extends DemoBaseEntity, ?> metamodelField = entityMetamodels.get(fields[i]);
// 			if (metamodelField == null) {
// 				// if metamodelField is null though populate method is called and entityclass is valid redis entity, then it implies input field is inherited class field as inherited fields are not populated in populate method 
// 				List<MetamodelField<?, ?>> metamodelFields = MetamodelUtils.getMetamodelFieldsForProperties(entityClass, List.of(fields[i]));
// 				if (metamodelFields != null && !metamodelFields.isEmpty()) {
// 					metamodelField = (MetamodelField<E, ?>) metamodelFields.get(0);
// 					entityMetamodels.put(fields[i], metamodelField);
// 					baseEntities_MetamodelFields.put(entityClass, entityMetamodels);				
// 				} else {
// 					throw new Exception("Entity : " + entityClass + " is not valid Redis Entity");
// 				}
// 			}
// 			returnMetamodelFields[i] =  ((MetamodelField<E, ?>) metamodelField);
// 		}
		
// 		return returnMetamodelFields;
// 	}
		
// 	public <E extends DemoBaseEntity> MetamodelField<E, ?> getMetamodelField(Class<E> entityClass, String field) throws Exception {
		
// 		return getMetamodelFields(entityClass, new String[] {field})[0];
// 	}
	
// }
