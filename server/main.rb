before do
  @title = 'goldfish'
end

get '/' do
  haml :index
end

get '/tag/:tag' do
  @tag = params[:tag]
  haml :tag
end
