- Remove deprecated method com.looseboxes.webform.web.WebRequest#getModelMap()

  Read the comments on the method and refactor to code to remove them method.
  Basically ModelMap should be accessed via AttributeStores.

- Cater for returning messages from api on success

  Currently, messages can only be returned from the api on error.

  On success, the response body is a FormConfig object
  To return messages on success, we may have to use a ResponseBody object for both
  success and error messages.
  ResponseBody{
      boolean error;
      String messages;
  }

  Remember it is response body so do not duplicate info in response like 
  http status code and message.

  Also check HTTP specifications/standards if we could rather use headers or 
  other means to send messages.


